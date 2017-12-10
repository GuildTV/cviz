using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using CViz.State;
using CViz.Timeline.Command;
using StilSoft.CasparCG.AmcpClient;

namespace CViz.Timeline
{
    class Timeline : ITimeline 
    {
        private readonly string _timelineId;
        private readonly AmcpConnection _client;
        private readonly int _channelId;
        private readonly List<Trigger> _remainingTriggers;
        private readonly List<Trigger> _activeTriggers; // TODO - make CopyOnWrite
        private readonly ConcurrentDictionary<int, LayerState> _currentLayerState;
        private HashSet<int> _usedLayers; // TODO - make readonly?

        private readonly object _triggerLock = new object();

        private readonly State.State _state;

        private Dictionary<string, string> _parameterValues;
        private bool _running = false;
        private bool _killNow = false;

        public Timeline(string timelineId, AmcpConnection client, int channelId, State.State state, List<Trigger> triggers)
        {
            _timelineId = timelineId;
            _client = client;
            _channelId = channelId;
            _state = state;
            _remainingTriggers = triggers;

            _activeTriggers = new List<Trigger>();
            _currentLayerState = new ConcurrentDictionary<int, LayerState>();
            _usedLayers = new HashSet<int>();

            state.SetState(TimelineState.Ready);
        }

        public int ChannelNumber => _channelId;
        
        public void Kill()
        {
            Console.WriteLine("Timeline " + _timelineId + " received kill");
            _killNow = true;
            _running = false;
        }

        public void SetLayerState(int layerId, LayerState state) => _currentLayerState[layerId] = state;
        public LayerState GetLayerState(int layerId) => _currentLayerState[layerId];

        public State.State State => _state;

        public List<Trigger> ActiveTriggers => _activeTriggers;

        public bool IsRunning => _running;

        public AmcpConnection Client => _client;

        private Trigger GetCueTrigger()
        {
            Trigger next = _activeTriggers.FirstOrDefault(t => !t.Loop);
            if (next == null)
                return null;

            if (next.Type == TriggerType.Cue)
                return next;

            return null;
        }

        private bool AreRequiredParametersDefined()
        {
            foreach (string fieldName in GetParameterNames(_remainingTriggers))
            {
                if (fieldName.IndexOf("@") == 0 && !_parameterValues.ContainsKey(fieldName.Substring(1)))
                {
                    _state.SetState(TimelineState.Error, "Missing required parameter: " + fieldName);
                    return false;
                }
            }
            return true;
        }
        
        public static HashSet<string> GetParameterNames(IEnumerable<Trigger> triggers)
        {
            return triggers.SelectMany(t => t.Commands.SelectMany(c => c.Parameters)).ToHashSet();
        }


        public void Run()
        {
            if (_running) return;
            _running = true;

            // check all required parameters are defined
            if (!AreRequiredParametersDefined())
            {
                _running = false;
                return;
            }

            // run any setup triggers
            long setupTriggerCount = _remainingTriggers.Count(t => t.Type == TriggerType.Cue);
            if (setupTriggerCount > 1)
            {
                Console.WriteLine("Timeline can only have one setup trigger");
                _running = false;
                return;
            }

            // collect the list of layers being altered
            _usedLayers = _remainingTriggers.SelectMany(t => t.Commands.Select(c => c.LayerId)).ToHashSet();

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

            _state.SetState(TimelineState.Run);

            Console.WriteLine("Starting timeline " + _timelineId);

            // set some triggers as active
            PromoteTriggersToActive();

            // Run the setup
            if (setupTrigger != null)
                ExecuteTrigger(setupTrigger);

            while (_running)
            {
                lock(_triggerLock) {
                    if (!_remainingTriggers.Any() && !_activeTriggers.Any())
                        break;
                }

                Trigger cueTrigger = GetCueTrigger();
                if (cueTrigger != null)
                    _state.SetState(TimelineState.Cue, cueTrigger.Name);
                else
                    _state.SetState(TimelineState.Run);

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
            if (_killNow)
            {
                _state.SetState(TimelineState.Error, "Killed");
                _remainingTriggers.Clear();
                _activeTriggers.Clear();
            }

            //ensure everything has been reset
            ClearAllUsedLayers();

            Console.WriteLine("Finished running timeline");
            _running = false;
            _state.SetState(TimelineState.Clear);
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
            int moved = 0;

            while (_remainingTriggers.Any())
            {
                Trigger t = _remainingTriggers.Pop();
                moved++;

                _activeTriggers.Add(t);

                // we only want to add up until a manual trigger
                if (t.Type == TriggerType.Cue)
                    break;
            }

            return moved > 0;
        }

        public void TriggerCue()
        {
            lock (_triggerLock)
            {
                if (!_running)
                {
                    Console.WriteLine("Received cue when not running");
                    return;
                }
                Console.WriteLine("Received a cue");
                // TODO - maybe this should be buffered, otherwise there could be some timing issues

                _state.SetState(TimelineState.Run);

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
            lock (_triggerLock)
            {
                if (!_running) return;

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
            // TODO - may want to look into using a thread to do the sending/commands, as they block until they get a response
            // may cause issues with integrity of remainingTriggers lists though
            trigger.Commands.ForEach(c => c.Execute(this));
            _activeTriggers.Remove(trigger);
        }

        public string GetParameterValue(string name, bool escape)
        {
            if (name.IndexOf("@") == 0)
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

            if (name.IndexOf("\"@") == 0 && name.LastIndexOf("\"") == name.Length - 1)
                return "\"" + GetParameterValue(name.Substring(1, name.Length - 1), escape) + "\"";

            return name;
        }

        internal void SetParameterValues(Dictionary<string, string> parameterValues)
        {
            if (_parameterValues != null)
                return;

            _parameterValues = parameterValues != null ? parameterValues : new Dictionary<string, string>();
        }

    }
}