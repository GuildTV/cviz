using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using CViz.State;
using CViz.Timeline.Command;
using CViz.Util;
using StilSoft.CasparCG.AmcpClient;

namespace CViz.Timeline
{
    class Timeline : ITimeline 
    {
        private readonly string _timelineId;
        private readonly List<Trigger> _remainingTriggers;
        private readonly List<Trigger> _activeTriggers;
        private readonly ConcurrentDictionary<int, LayerState> _currentLayerState;
        private readonly HashSet<int> _usedLayers;

        private Dictionary<string, string> _parameterValues;

        public Timeline(string timelineId, AmcpConnection client, int channelId, State.State state, List<Trigger> triggers)
        {
            _timelineId = timelineId;
            Client = client;
            ChannelNumber = channelId;
            State = state;
            _remainingTriggers = triggers;

            _activeTriggers = new List<Trigger>();
            _currentLayerState = new ConcurrentDictionary<int, LayerState>();
            _usedLayers = new HashSet<int>();

            state.SetState(TimelineState.Ready);
        }

        public int ChannelNumber { get; }
        public State.State State { get; }
        public AmcpConnection Client { get; }
        public bool IsRunning { get; private set; }
        private bool KillNow { get; set; }

        public void Kill()
        {
            Console.WriteLine("Timeline " + _timelineId + " received kill");
            KillNow = true;
            IsRunning = false;
        }

        public void SetLayerState(int layerId, LayerState state) => _currentLayerState[layerId] = state;
        public LayerState GetLayerState(int layerId) => _currentLayerState[layerId];

        
        private Trigger GetCueTrigger()
        {
            lock (_activeTriggers)
            {
                Trigger next = _activeTriggers.FirstOrDefault(t => !t.Loop);

                if (next?.Type == TriggerType.Cue)
                    return next;

                return null;
            }
        }

        private bool AreRequiredParametersDefined(IEnumerable<Trigger> triggers)
        {
            foreach (string fieldName in triggers.SelectMany(t => t.Commands).SelectMany(c => c.Parameters).Distinct())
            {
                if (fieldName.IndexOf("@", StringComparison.InvariantCulture) == 0 && !_parameterValues.ContainsKey(fieldName.Substring(1)))
                {
                    State.SetState(TimelineState.Error, "Missing required parameter: " + fieldName);
                    return false;
                }
            }
            return true;
        }
        
        public static HashSet<string> GetParameterNames(IEnumerable<Trigger> triggers)
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
            long setupTriggerCount = _remainingTriggers.Count(t => t.Type == TriggerType.Cue);
            if (setupTriggerCount > 1)
            {
                Console.WriteLine("Timeline can only have one setup trigger");
                IsRunning = false;
                return;
            }

            // collect the list of layers being altered
            _usedLayers.AddRange(_remainingTriggers.SelectMany(t => t.Commands).Select(c => c.LayerId));

            Console.WriteLine("Template spans " + _usedLayers.Count + " layers");

            Trigger setupTrigger = _remainingTriggers.FirstOrDefault();
            if (setupTrigger != null && setupTrigger.Type == TriggerType.Setup)
            {
                _remainingTriggers.RemoveAt(0);
            }
            else
            {
                setupTrigger = null;
            }

            State.SetState(TimelineState.Run);

            Console.WriteLine("Starting timeline " + _timelineId);

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

                Trigger cueTrigger = GetCueTrigger();
                if (cueTrigger != null)
                    State.SetState(TimelineState.Cue, cueTrigger.Name);
                else
                    State.SetState(TimelineState.Run);

                // wait until the timeline has been finished
                try
                {
                    Thread.Sleep(10);
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.StackTrace);
                }
            }

            // if kill command has been sent, then wipe everything
            if (KillNow)
            {
                State.SetState(TimelineState.Error, "Killed");
                _remainingTriggers.Clear();
                // ReSharper disable once InconsistentlySynchronizedField
                _activeTriggers.Clear();
            }

            //ensure everything has been reset
            ClearAllUsedLayers();

            Console.WriteLine("Finished running timeline");
            IsRunning = false;
            State.SetState(TimelineState.Clear);
        }

        private void ClearAllUsedLayers()
        {
            foreach (int l in _usedLayers)
            {
                CommandBase c = new ClearCommand(l);
                c.Execute(this);
                Console.WriteLine("Clearing layer " + l);
            }
        }

        private bool PromoteTriggersToActive()
        {
            lock (_activeTriggers)
            {
                int moved = 0;

                while (_remainingTriggers.Any())
                {
                    var t =_remainingTriggers.FirstOrDefault();
                    if (t == null)
                        break;

                    moved++;

                    _remainingTriggers.RemoveAt(0);
                    _activeTriggers.Add(t);

                    // we only want to add up until a manual trigger
                    if (t.Type == TriggerType.Cue)
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
                    Console.WriteLine("Received cue when not running");
                    return;
                }
                Console.WriteLine("Received a cue");
                // TODO - maybe this should be buffered, otherwise there could be some timing issues

                State.SetState(TimelineState.Run);

                // find trigger to cue
                Trigger waiting = GetCueTrigger();
                if (waiting == null)
                {
                    Console.WriteLine("Received a cue without a trigger to fire");
                    return;
                }

                // run the trigger
                ExecuteTrigger(waiting);
                _activeTriggers.Remove(waiting);

                if (!PromoteTriggersToActive())
                {
                    Console.WriteLine("Reached end of timeline");
                }
            }
        }

        internal void TriggerOnVideoFrame(int layer, long frame, long totalFrames)
        {
            lock (_activeTriggers)
            {
                if (!IsRunning) return;

                foreach (Trigger t in _activeTriggers)
                {
                    if (t.Type == TriggerType.Setup)
                        continue;

                    if (t.LayerId != layer)
                        continue;

                    // determine the frame we are aiming for
                    long targetFrame = totalFrames;
                    if (t.Type != TriggerType.End)
                    {
                        targetFrame = t.TargetFrame;
                    }

                    LayerState state = _currentLayerState[layer];
                    if (state == null)
                    {
                        Console.WriteLine("Tried to get state and failed");
                    }
                    // TODO - this check needs to ensure that an appropriate amount of time has passed
                    // NOTE: this also gets hit if the source video is a different framerate to the channel
                    else if (state.LastFrame == frame && targetFrame > frame)
                    {
                        // the video didn't play to the end for some reason, move on
                        Console.WriteLine("Loop didn't reach the end, check your video!");

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

                LayerState st;
                if (_currentLayerState.TryGetValue(layer, out st))
                    st.LastFrame = frame;
            }
        }

        private void ExecuteTrigger(Trigger trigger)
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

        internal void SetParameterValues(Dictionary<string, string> parameterValues)
        {
            if (_parameterValues != null)
                return;

            _parameterValues = parameterValues ?? new Dictionary<string, string>();
        }


        public void AddTrigger(Trigger t)
        {
            lock (_activeTriggers)
                _activeTriggers.Add(t);
        }

        public void RemoveAllTriggers(Predicate<Trigger> predicate)
        {
            lock (_activeTriggers)
                _activeTriggers.RemoveAll(predicate);
        }

    }
}