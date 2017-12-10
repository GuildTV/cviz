using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading;
using CViz.Config;
using CViz.Control;
using CViz.Timeline;
using StilSoft.CasparCG.AmcpClient;

namespace CViz
{
    class TimelineManager
    {
        public static string TimelineExt = ".tl";

        private readonly AmcpConnection _client;
        private readonly Config.Config _config;
        private readonly Dictionary<string, Timeline.Timeline> _timelines;

        private IControlInterface _controlInterface;

        public TimelineManager(Config.Config config)
        {
            _config = config;
            OscWrapper oscWrapper = new OscWrapper(this, config.OscPort);
            new Thread(oscWrapper.Run).Start();

            _client = new AmcpConnection(config.CasparHost, config.CasparPort)
            {

                AutoConnect = true,
                AutoReconnect = true,
                KeepAliveEnable = true

            };
            _timelines = new Dictionary<string, Timeline.Timeline>();
        }

        public void BindInterface(IControlInterface newInterface)
        {
            _controlInterface = newInterface;
        }

        public IReadOnlyList<State.State> GetCompleteState()
        {
            lock (_timelines)
            {
                return _timelines.Select(t => t.Value.State).ToList();
            }
        }

        public State.State GetStateForTimelineSlot(String timelineSlot)
        {
            lock (_timelines)
            {
                return _timelines.Select(t => t.Value.State).FirstOrDefault(s => s.TimelineSlot == timelineSlot);
            }
        }
        
        public bool LoadTimeline(string channelId, string timelineSlot, string filename, string instanceId)
        {
            lock (_timelines)
            {
                Timeline.Timeline timeline = _timelines[timelineSlot];
                if (timeline != null && timeline.IsRunning)
                {
                    Console.WriteLine("Cannot load timeline to " + timelineSlot + " when one is already running");
                    return false;
                }

                if (timeline != null)
                    _timelines.Remove(channelId);

                ChannelConfig channelConfig = _config.GetChannelById(channelId);
                if (channelConfig == null)
                {
                    Console.WriteLine("Channel " + channelId + " is not defined in config");
                    return false;
                }
                
                string fullPath = Path.GetFullPath(Path.Combine(_config.TemplateDir, filename + TimelineExt));
                if (!File.Exists(fullPath) || !File.GetAttributes(fullPath).HasFlag(FileAttributes.Directory))
                {
                    Console.WriteLine("Cannot find new timeline file: " + filename);
                    return false;
                }

                List<Trigger> sequence;
                try
                {
                    sequence = Parser.ParseFile(fullPath);
                }
                catch (Exception)
                {
                    Console.WriteLine("Failed to parse timeline file: " + filename);
                    return false;
                }

                State.State state = new State.State(_controlInterface, timelineSlot, filename, instanceId);
                _timelines[timelineSlot] = new Timeline.Timeline(timelineSlot, _client, channelConfig.Channel, state, sequence);

                Console.WriteLine("Timeline " + timelineSlot + "ready");

                return true;
            }
        }

        public bool StartTimeline(string timelineId, Dictionary<string, string> parameters)
        {
            lock (_timelines)
            {
                Timeline.Timeline timeline = _timelines[timelineId];
                if (timeline == null || timeline.IsRunning)
                    return false;

                timeline.SetParameterValues(parameters);

                new Thread(timeline.Run).Start();
                return true;
            }
        }

        public void KillTimeline(String timelineId)
        {
            lock (_timelines)
            {
                _timelines[timelineId]?.Kill();
            }
        }

        public void TriggerCue(String timelineId)
        {
            lock (_timelines)
            {
                _timelines[timelineId]?.TriggerCue();
            }
        }

        internal void TriggerOnVideoFrame(int channel, int layer, long frame, long totalFrames)
        {
            lock (_timelines)
            {
                if (_timelines == null)
                    return;

                foreach (Timeline.Timeline timeline in _timelines.Values.Where(t => t.ChannelNumber == channel))
                {
                    timeline.TriggerOnVideoFrame(layer, frame, totalFrames);
                }
            }
        }
    }
}