using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Text;
using System.Threading;
using CViz.Timeline.Command;
using CViz.Timeline.Triggers;
using CViz.Util;
using log4net;
using LibAtem.Net;
using StilSoft.CasparCG.AmcpClient;

namespace CViz.Timeline
{
    class Timeline : ITimeline
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(Timeline));

        private readonly string _timelineId;
        private readonly TimelineSpec _spec;
        private readonly ConcurrentDictionary<int, LayerState> _currentLayerState;
        private readonly HashSet<int> _usedLayers;

        private TimelineTriggerSet _previousTriggers;
        private TimelineTriggerSet _triggers;
        private readonly object _triggersLock;
        
        private int _portId;
        private long _portFirstFrame;

        public Timeline(string timelineId, AmcpConnection casparClient, AtemClient atemClient, int channelId, TimelineState state, TimelineSpec spec)
        {
            _timelineId = timelineId;
            CasparClient = casparClient;
            AtemClient = atemClient;
            ChannelNumber = channelId;
            State = state;
            _spec = spec;

            _triggersLock = new object();
            _triggers = new TimelineTriggerSet(spec.Triggers.ToList(), new List<ITrigger>(), null);

            _currentLayerState = new ConcurrentDictionary<int, LayerState>();
            _usedLayers = new HashSet<int>();

            state.SetState(TimelineState.StateType.Ready);
        }
        
        public int ChannelNumber { get; }
        public TimelineState State { get; }

        public AmcpConnection CasparClient { get; }
        public AtemClient AtemClient { get; }

        public bool IsRunning { get; private set; }
        private bool KillNow { get; set; }

        public void Kill()
        {
            Log.InfoFormat("Killing timeline: {0}", _timelineId);
            KillNow = true;
            IsRunning = false;
        }

        public void SetLayerState(int layerId, LayerState state) => _currentLayerState[layerId] = state;
        public LayerState GetLayerState(int layerId) => _currentLayerState.TryGetValue(layerId, out LayerState val) ? val : null;
        
        private CueTrigger GetCueTrigger()
        {
            lock (_triggersLock)
            {
                ITrigger next = _triggers.Active.FirstOrDefault(t => !(t is LoopTrigger));
                return next as CueTrigger;
            }
        }

        private bool AreRequiredParametersDefined(TimelineTriggerSet triggerSet)
        {
            foreach (string fieldName in triggerSet.Remaining.SelectMany(t => t.Commands).SelectMany(c => c.Parameters).Distinct())
            {
                if (fieldName.IndexOf("@", StringComparison.InvariantCulture) == 0 && !triggerSet.ParameterValues.ContainsKey(fieldName.Substring(1)))
                {
                    State.SetState(TimelineState.StateType.Error, "Missing required parameter: " + fieldName);
                    return false;
                }
            }
            return true;
        }
        
        public static HashSet<string> GetParameterNames(IEnumerable<ITrigger> triggers)
        {
            return new HashSet<string>(triggers.SelectMany(t => t.Commands).SelectMany(c => c.Parameters));
        }
        
        public void Run()
        {
            if (IsRunning) return;
            IsRunning = true;

            // check all required parameters are defined
            if (!AreRequiredParametersDefined(_triggers))
            {
                IsRunning = false;
                return;
            }

            // collect the list of layers being altered
            _usedLayers.AddRange(_triggers.Remaining.SelectMany(t => t.Commands).Select(c => c.LayerId).Where(l => l > 0));

            Log.InfoFormat("Template spans {0} layers", _usedLayers.Count);

            ITrigger setupTrigger = _triggers.Remaining.OfType<SetupTrigger>().FirstOrDefault();
            if (setupTrigger != null)
                _triggers.Remaining.Remove(setupTrigger);

            State.SetState(TimelineState.StateType.Run);

            Log.InfoFormat("Starting timeline: {0}", _timelineId);

            // set some triggers as active
            PromoteTriggersToActive();

            // Run the setup
            if (setupTrigger != null)
                ExecuteTrigger(setupTrigger);

            while (IsRunning)
            {
                lock(_triggersLock) {
                    if (!_triggers.Remaining.Any() && _triggers.Active.All(t => (t is LoopTrigger)))
                    {
                        if (_previousTriggers != null)
                            SwapBackToMainTimeline();
                        else
                            break;
                    }
                }

                CueTrigger cueTrigger = GetCueTrigger();
                if (cueTrigger != null)
                    State.SetState(
                        cueTrigger is RunChildOrCueTrigger
                            ? TimelineState.StateType.CueOrChild
                            : TimelineState.StateType.Cue, cueTrigger.Name);
                else
                    State.SetState(TimelineState.StateType.Run);

                // wait until the timeline has been finished
                try
                {
                    Thread.Sleep(10);
                }
                catch (Exception)
                {
                }
            }

            // if kill command has been sent, then wipe everything
            if (KillNow)
            {
                State.SetState(TimelineState.StateType.Error, "Killed");
                _triggers.Remaining.Clear();
                _triggers.Active.Clear();
            }

            //ensure everything has been reset
            ClearAllUsedLayers();

            Log.InfoFormat("Finished timeline: {0}", _timelineId);
            IsRunning = false;
            State.SetState(TimelineState.StateType.Clear);
        }

        private void ClearAllUsedLayers()
        {
            List<int> layers = _usedLayers.ToList();
            
            foreach (int l in layers)
            {
                CommandBase c = new ClearCommand(l);
                c.Execute(this);
                Log.InfoFormat("Clearing layer: {0}", l);
            }
        }

        private bool PromoteTriggersToActive()
        {
            lock (_triggersLock)
            {
                int moved = 0;

                while (_triggers.Remaining.Any())
                {
                    ITrigger t = _triggers.Remaining.FirstOrDefault();
                    if (t == null)
                        break;

                    moved++;

                    _triggers.Remaining.RemoveAt(0);

                    _triggers.Active.Add(t);
                    
                    // we only want to add up until a manual trigger
                    if (t is CueTrigger || t is DelayTrigger)
                        break;
                }

                if (moved == 0 && _previousTriggers != null)
                {
                    SwapBackToMainTimeline();
                    return true;
                }

                return moved > 0;
            }
        }

        private void SwapBackToMainTimeline()
        {
            lock (_triggersLock)
            {
                _previousTriggers.Active.AddRange(_triggers.Active.Where(t => t is LoopTrigger));
                _triggers = _previousTriggers;
                _previousTriggers = null;
            }
        }

        public void TriggerChild(string name, Dictionary<string, string> parameters)
        {
            lock (_triggersLock)
            {
                if (!IsRunning)
                {
                    Log.InfoFormat("Recevied runchild when not running");
                    return;
                }
                Log.InfoFormat("Recevied runchild for: {0}", _timelineId);

                State.SetState(TimelineState.StateType.Run);

                // find trigger to cue
                RunChildOrCueTrigger waiting = GetCueTrigger() as RunChildOrCueTrigger;
                if (waiting == null)
                {
                    Log.InfoFormat("Recevied runchild with no trigger to fire: {0}", _timelineId);
                    return;
                }

                if (!_spec.ChildTimelines.TryGetValue(waiting.TimelineName, out ImmutableArray<ITrigger> newTriggers))
                {
                    Log.InfoFormat("Timeline is corrupt and is missing child timeline data: {0}", _timelineId);
                    return;
                }

                var newTriggerSet = new TimelineTriggerSet(newTriggers.Select(t => t.Clone()).ToList(),
                    _triggers.Active.Where(t => t is LoopTrigger).ToList(), parameters.ToImmutableDictionary());

                // check all required parameters are defined
                if (!AreRequiredParametersDefined(newTriggerSet)) // TODO fix this usage! _parameters havent been upated yet
                {
                    Log.WarnFormat("Not all parameters to child timeline are defined. Ignoring run");
                    return;
                }

                // Promote triggers
                _previousTriggers = _triggers;
                _triggers = newTriggerSet;
                State.InstanceName = name;

                _previousTriggers.Active.RemoveAll(t => t is LoopTrigger);
                
                // collect the list of layers being altered
                _usedLayers.AddRange(_triggers.Remaining.SelectMany(t => t.Commands).Select(c => c.LayerId).Where(l => l > 0));

                ITrigger setupTrigger = _triggers.Remaining.OfType<SetupTrigger>().FirstOrDefault();
                if (setupTrigger != null)
                    _triggers.Remaining.Remove(setupTrigger);

                State.SetState(TimelineState.StateType.Run);

                Log.InfoFormat("Starting child timeline: {0}", waiting.TimelineName);

                // set some triggers as active
                PromoteTriggersToActive();

                // Run the setup
                if (setupTrigger != null)
                    ExecuteTrigger(setupTrigger);
            }
        }

        public void TriggerCue()
        {
            lock (_triggersLock)
            {
                if (!IsRunning)
                {
                    Log.InfoFormat("Recevied cue when not running");
                    return;
                }
                Log.InfoFormat("Recevied cue for: {0}", _timelineId);
                // TODO - maybe this should be buffered, otherwise there could be some timing issues

                State.SetState(TimelineState.StateType.Run);

                // find trigger to cue
                CueTrigger waiting = GetCueTrigger();
                if (waiting == null)
                {
                    Log.InfoFormat("Recevied cue with no trigger to fire: {0}", _timelineId);
                    return;
                }

                // run the trigger
                ExecuteTrigger(waiting);

                if (!PromoteTriggersToActive())
                {
                    Log.InfoFormat("Reached end of timeline: {0}", _timelineId);
                }
            }
        }

        internal void TriggerOnLayerFrame(int layer, LayerType type, long frame, long totalFrames)
        {
            lock (_triggersLock)
            {
                if (!IsRunning) return;

                // TODO - this could be rewritten to always keep currentLayerState up to date, as some triggers may need the info. but changing the timeline can fix that
                foreach (FrameTrigger t in _triggers.Active.OfType<FrameTrigger>().ToList())
                {
                    if (t.Layer != layer)
                        continue;

                    // determine the frame we are aiming for
                    long targetFrame = totalFrames;
                    if (t.TargetFrame != -1)
                        targetFrame = t.TargetFrame;

                    if (!_currentLayerState.TryGetValue(layer, out LayerState state) && type == LayerType.Scene)
                    {
                        state = new LayerState(type, "");
                        _currentLayerState[layer] = state;
                    }

                    if (state == null)
                    {
                        Log.InfoFormat("Failed to get layer state: {0}-{1}", _timelineId, layer);
                    }
                    else if (state.Type != type)
                    {
                        Log.WarnFormat("Mismatched layer typee: {0}-{1}", _timelineId, layer);
                    }
                    // TODO - this check needs to ensure that an appropriate amount of time has passed
                    // NOTE: this also gets hit if the source video is a different framerate to the channel
                    else if (state.LastFrame == frame && targetFrame > frame)
                    {
                        // the video didn't play to the end for some reason, move on
                        if (state.Type != LayerType.Scene || targetFrame - 1 != frame)
                            Log.InfoFormat("Loop didn't reach the end, check your video! {0}-{1}", _timelineId, layer);

                        ExecuteTrigger(t);
                    }
                    else if (t.Waited)
                    {
                        // do it
                        ExecuteTrigger(t);

                    }
                    else if (frame >= targetFrame)
                    {
                        t.SetWaited();
                    }
                }

                if (_currentLayerState.TryGetValue(layer, out LayerState st))
                    st.LastFrame = frame;
            }
        }
        
        internal void TriggerOnScenePaused(int layer, string stopName)
        {
            lock (_triggersLock)
            {
                if (!IsRunning) return;

                foreach (SceneStopTrigger t in _triggers.Active.OfType<SceneStopTrigger>().ToList())
                {
                    if (t.Layer != layer)
                        continue;

                    if (t.StopName != stopName)
                        continue;

                    ExecuteTrigger(t);
                }
            }
        }

        internal void TriggerOnChannelFrame(int port, long frame)
        {
            // Use the first port that is found
            if (_portId == 0)
                _portId = port;
            if (_portId != port)
                return;

            if (_portFirstFrame == 0)
                _portFirstFrame = frame;

            if (frame <= _portFirstFrame)
                return;

            lock (_triggersLock)
            {
                if (!IsRunning)
                    return;

                // find trigger to cue
                DelayTrigger next = _triggers.Active.FirstOrDefault(t => !(t is LoopTrigger)) as DelayTrigger;
                if (next == null)
                    return;

                // If the counter hasnt started, then set it as starting on the previous frame
                if (!next.DelayStarted)
                    next.StartDelay(frame - 1);

                // Check if hit the target yet
                if (next.DelayEndAt > frame)
                    return;

                ExecuteTrigger(next);

                if (!PromoteTriggersToActive())
                {
                    Log.InfoFormat("Reached end of timeline: {0}", _timelineId);
                }
            }
        }

        private void ExecuteTrigger(ITrigger trigger)
        {
            lock (_triggersLock)
            {
                // TODO - may want to look into using a thread to do the sending/commands, as they block until they get a response
                // may cause issues with integrity of remainingTriggers lists though
                trigger.Commands.ForEach(c => c.Execute(this));
                _triggers.Active.Remove(trigger);
            }
        }
        
        public string GetParameterValue(string name, bool escape)
        {
            if (name.IndexOf("@", StringComparison.InvariantCulture) == 0)
            {
                if (!_triggers.ParameterValues.TryGetValue(name.Substring(1), out string param))
                    return name;

                if (escape)
                    param = param.AddSlashes();

                return param;
            }

            if (name.IndexOf("\"@", StringComparison.InvariantCulture) == 0 && name.LastIndexOf("\"", StringComparison.InvariantCulture) == name.Length - 1)
                return "\"" + GetParameterValue(name.Substring(1, name.Length - 1), escape) + "\"";

            return name;
        }

        internal void SetParameterValues(ImmutableDictionary<string, string> parameterValues)
        {
            _triggers.SetParameters(parameterValues);
        }


        public void AddTrigger(ITrigger t)
        {
            lock (_triggersLock)
                _triggers.Active.Add(t);
        }

        public void RemoveAllTriggers(Predicate<ITrigger> predicate)
        {
            lock (_triggersLock)
                _triggers.Active.RemoveAll(predicate);
        }
        
    }
}