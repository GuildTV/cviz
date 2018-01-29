using System;
using System.Net;
using System.Text.RegularExpressions;
using Bespoke.Common;
using Bespoke.Common.Osc;
using log4net;

namespace CViz
{
    class OscWrapper
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(OscWrapper));

        private readonly TimelineManager _manager;
        private readonly int _oscPort;
        private readonly OscServer _server;

        public OscWrapper(TimelineManager manager, int oscPort)
        {
            _manager = manager;
            _oscPort = oscPort;

            _server = new OscServer(TransportType.Udp, IPAddress.Any, oscPort);
            _server.FilterRegisteredMethods = false;
            _server.MessageReceived += MessageReceived;
            _server.ReceiveErrored += ReceiveError;
            _server.ConsumeParsingExceptions = false;
        }

        public void Run()
        {
            Log.InfoFormat("Starting OSC Listener on port: {0}", _oscPort);
            _server.Start();
        }

        private void MessageReceived(object sender, OscMessageReceivedEventArgs e)
        {
            try
            {
                OscMessage message = e.Message;
                Match match = Regex.Match(message.Address, "/channel/([0-9]+)/stage/layer/([0-9]+)/file/frame");
                if (match.Success)
                {
                    ParseVideoFrameMessage(message, match);
                    return;
                }

                match = Regex.Match(message.Address, "/channel/([0-9]+)/stage/layer/([0-9]+)/scene/timeline-frame"); // TODO - this may want correcting, as the name used in caspar feels wrong
                if (match.Success)
                {
                    ParseSceneFrameMessage(message, match);
                    return;
                }

                match = Regex.Match(message.Address, "/channel/([0-9]+)/stage/layer/([0-9]+)/scene/paused");
                if (match.Success)
                {
                    ParseScenePausedMessage(message, match);
                    return;
                }

                match = Regex.Match(message.Address, "/channel/([0-9]+)/output/port/([0-9]+)/frame");
                if (match.Success)
                {
                    ParseChannelFrameMessage(message, match);
                    return;
                }
                
            }
            catch (Exception ex)
            {
                Log.ErrorFormat("Failed to parse: {0}", ex.Message);
            }
        }

        private void ParseVideoFrameMessage(OscMessage message, Match match)
        {
            int channel = int.Parse(match.Groups[1].Value);
            int layer = int.Parse(match.Groups[2].Value);

            long frame = (long)message.Data[0];
            long totalFrames = (long)message.Data[1];

//            Log.InfoFormat("Video progress: {0}-{1} {2}/{3}", channel, layer, frame, totalFrames);
            _manager.TriggerOnLayerFrame(channel, layer, LayerType.Video, frame, totalFrames);
        }

        private void ParseSceneFrameMessage(OscMessage message, Match match)
        {
            int channel = int.Parse(match.Groups[1].Value);
            int layer = int.Parse(match.Groups[2].Value);

            long frame = (long)message.Data[0];
            long totalFrames = (long)message.Data[1];

            _manager.TriggerOnLayerFrame(channel, layer, LayerType.Scene, frame, totalFrames);
        }

        private void ParseScenePausedMessage(OscMessage message, Match match)
        {
            int channel = int.Parse(match.Groups[1].Value);
            int layer = int.Parse(match.Groups[2].Value);

            bool paused = (bool)message.Data[0];
            string stopName = (string)message.Data[1];

            if (paused)
                _manager.TriggerOnScenePaused(channel, layer, stopName);
        }

        private void ParseChannelFrameMessage(OscMessage message, Match match)
        {
            int channel = int.Parse(match.Groups[1].Value);
            int port = int.Parse(match.Groups[2].Value);

            long frame = (long)message.Data[0];

//            Log.InfoFormat("Channel frame: {0}-{1} {2}", channel, port, frame);
            _manager.TriggerOnChannelFrame(channel, port, frame);
        }

        private static void ReceiveError(object sender, ExceptionEventArgs e)
        {
            Log.ErrorFormat("Failed to receive packet: {0}", e.Exception.Message);
        }
    }
}