using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Threading;
using CViz.Timeline.Command;
using CViz.Timeline.Triggers;
using CViz.Util;
using log4net;
using StilSoft.CasparCG.AmcpClient;

namespace CViz.Timeline
{
    class Timeline : ITimeline
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(Timeline));

        private readonly string _timelineId;
        private readonly List<ITrigger> _remainingTriggers;
        private readonly List<ITrigger> _activeTriggers;
        private readonly ConcurrentDictionary<int, LayerState> _currentLayerState;
        private readonly HashSet<int> _usedLayers;

        private int _portId;
        private long _portFirstFrame;

        private ImmutableDictionary<string, string> _parameterValues;

        public Timeline(string timelineId, AmcpConnection client, int channelId, TimelineState state, List<ITrigger> triggers)
        {
            _timelineId = timelineId;
            Client = client;
            ChannelNumber = channelId;
            State = state;
            _remainingTriggers = triggers;

            _activeTriggers = new List<ITrigger>();
            _currentLayerState = new ConcurrentDictionary<int, LayerState>();
            _usedLayers = new HashSet<int>();

            state.SetState(TimelineState.StateType.Ready);
        }
        
        public int ChannelNumber { get; }
        public TimelineState State { get; }
        public AmcpConnection Client { get; }
        public bool IsRunning { get; private set; }
        private bool KillNow { get; set; }

        public void Kill()
        {
            Log.InfoFormat("Killing timeline: {0}", _timelineId);
            KillNow = true;
            IsRunning = false;
        }

        public void SetLayerState(int layerId, LayerState state) => _currentLayerState[layerId] = state;
        public LayerState GetLayerState(int layerId) => _currentLayerState[layerId];
        
        private CueTrigger GetCueTrigger()
        {
            lock (_activeTriggers)
            {
                ITrigger next = _activeTriggers.FirstOrDefault(t => !(t is LoopTrigger));
                return next as CueTrigger;
            }
        }

        private bool AreRequiredParametersDefined(IEnumerable<ITrigger> triggers)
        {
            foreach (string fieldName in triggers.SelectMany(t => t.Commands).SelectMany(c => c.Parameters).Distinct())
            {
                if (fieldName.IndexOf("@", StringComparison.InvariantCulture) == 0 && !_parameterValues.ContainsKey(fieldName.Substring(1)))
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
            if (!AreRequiredParametersDefined(_remainingTriggers))
            {
                IsRunning = false;
                return;
            }

            // run any setup triggers
            long setupTriggerCount = _remainingTriggers.Count(t => t is SetupTrigger);
            if (setupTriggerCount > 1)
            {
                Log.WarnFormat("Timeline can only have one setup trigger");
                State.SetState(TimelineState.StateType.Error, "Timeline invalid");
                IsRunning = false;
                return;
            }

            // collect the list of layers being altered
            _usedLayers.AddRange(_remainingTriggers.SelectMany(t => t.Commands).Select(c => c.LayerId).Where(l => l > 0));

            Log.InfoFormat("Template spans {0} layers", _usedLayers.Count);

            ITrigger setupTrigger = _remainingTriggers.OfType<SetupTrigger>().FirstOrDefault();
            if (setupTrigger != null)
                _remainingTriggers.Remove(setupTrigger);

            State.SetState(TimelineState.StateType.Run);

            Log.InfoFormat("Starting timeline: {0}", _timelineId);

            // set some triggers as active
            PromoteTriggersToActive();

            // Run the setup
            if (setupTrigger != null)
                ExecuteTrigger(setupTrigger);

            while (IsRunning)
            {
                lock(_activeTriggers) {
                    if (!_remainingTriggers.Any() && !_activeTriggers.Any())
                        break;
                }

                CueTrigger cueTrigger = GetCueTrigger();
                if (cueTrigger != null)
                    State.SetState(TimelineState.StateType.Cue, cueTrigger.Name);
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
                _remainingTriggers.Clear();
                // ReSharper disable once InconsistentlySynchronizedField
                _activeTriggers.Clear();
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
            lock (_activeTriggers)
            {
                int moved = 0;

                while (_remainingTriggers.Any())
                {
                    ITrigger t =_remainingTriggers.FirstOrDefault();
                    if (t == null)
                        break;

                    moved++;

                    _remainingTriggers.RemoveAt(0);
                    
                    _activeTriggers.Add(t);
                    
                    // we only want to add up until a manual trigger
                    if (t is CueTrigger)
                        break;
                }

                return moved > 0;
            }
        }

        public void TriggerCue()
        {
            lock (_activeTriggers)
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

        internal void TriggerOnVideoFrame(int layer, long frame, long totalFrames)
        {
            lock (_activeTriggers)
            {
                if (!IsRunning) return;

                foreach (FrameTrigger t in _activeTriggers.OfType<FrameTrigger>())
                {
                    if (t.Layer != layer)
                        continue;

                    // determine the frame we are aiming for
                    long targetFrame = totalFrames;
                    if (t.TargetFrame == -1)
                        targetFrame = t.TargetFrame;

                    if (!_currentLayerState.TryGetValue(layer, out LayerState state))
                    {
                        Log.InfoFormat("Failed to get layer state: {0}-{1}", _timelineId, layer);
                    }
                    // TODO - this check needs to ensure that an appropriate amount of time has passed
                    // NOTE: this also gets hit if the source video is a different framerate to the channel
                    else if (state.LastFrame == frame && targetFrame > frame)
                    {
                        // the video didn't play to the end for some reason, move on
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

            lock (_activeTriggers)
            {
                if (!IsRunning)
                    return;

                // find trigger to cue
                DelayTrigger next = _activeTriggers.FirstOrDefault(t => !(t is LoopTrigger)) as DelayTrigger;

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
            lock (_activeTriggers)
            {
                // TODO - may want to look into using a thread to do the sending/commands, as they block until they get a response
                // may cause issues with integrity of remainingTriggers lists though
                trigger.Commands.ForEach(c => c.Execute(this));
                _activeTriggers.Remove(trigger);
            }
        }
        
        public string GetParameterValue(string name, bool escape)
        {
            if (name.IndexOf("@", StringComparison.InvariantCulture) == 0)
            {
                if (!_parameterValues.TryGetValue(name.Substring(1), out string param))
                    return name;

                if (escape)
                {
                    param = param.Replace("\\n", "\\\\n"); // " => \"
                    param = param.Replace("\\\"", "\\\\\\\\\""); // \" => \\\\"
                    param = param.Replace("\"", "\\\""); // " => \"
                }

                return param;
            }

            if (name.IndexOf("\"@", StringComparison.InvariantCulture) == 0 && name.LastIndexOf("\"", StringComparison.InvariantCulture) == name.Length - 1)
                return "\"" + GetParameterValue(name.Substring(1, name.Length - 1), escape) + "\"";

            return name;
        }

        internal void SetParameterValues(ImmutableDictionary<string, string> parameterValues)
        {
            if (_parameterValues != null)
                return;

            _parameterValues = parameterValues ?? ImmutableDictionary<string, string>.Empty;
        }


        public void AddTrigger(ITrigger t)
        {
            lock (_activeTriggers)
                _activeTriggers.Add(t);
        }

        public void RemoveAllTriggers(Predicate<ITrigger> predicate)
        {
            lock (_activeTriggers)
                _activeTriggers.RemoveAll(predicate);
        }
    }
}