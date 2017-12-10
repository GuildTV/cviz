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
                if (!match.Success)
                    return;

                int channel = int.Parse(match.Groups[1].Value);
                int layer = int.Parse(match.Groups[2].Value);

                long frame = (long) message.Data[0];
                long totalFrames = (long) message.Data[1];

                Log.InfoFormat("Video progress: {0}-{1} {2}/{3}", channel, layer, frame, totalFrames);
                _manager.TriggerOnVideoFrame(channel, layer, frame, totalFrames);
            }
            catch (Exception ex)
            {
                Log.ErrorFormat("Failed to parse: {0}", ex.Message);
            }
        }

        private static void ReceiveError(object sender, ExceptionEventArgs e)
        {
            Log.ErrorFormat("Failed to receive packet: {0}", e.Exception.Message);
        }
    }
}