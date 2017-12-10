using System;
using System.Net;
using System.Text.RegularExpressions;
using Bespoke.Common;
using Bespoke.Common.Osc;

namespace CViz
{
    class OscWrapper
    {
        private readonly TimelineManager _manager;
        private readonly OscServer _server;

        public OscWrapper(TimelineManager manager, int oscPort)
        {
            _manager = manager;

            _server = new OscServer(TransportType.Udp, IPAddress.Any, oscPort);
            _server.FilterRegisteredMethods = false;
            _server.MessageReceived += MessageReceived;
            _server.ReceiveErrored += ReceiveError;
            _server.ConsumeParsingExceptions = false;
        }

        public void Run()
        {
            _server.Start();
        }

        private void MessageReceived(object sender, OscMessageReceivedEventArgs e)
        {
            OscMessage message = e.Message;

            Match match = Regex.Match(message.Address, "/channel/([0-9]+)/stage/layer/([0-9]+)/file/frame");
            if (!match.Success)
                return;

            try
            {

                int channelNumber = int.Parse(match.Groups[1].Value);
                int layer = int.Parse(match.Groups[2].Value);

                long frame = (long) message.Data[0];
                long totalFrames = (long) message.Data[1];

                _manager.TriggerOnVideoFrame(channelNumber, layer, frame, totalFrames);
            }
            catch (Exception ex)
            {
                Console.WriteLine("Exception: " + ex.Message);
            }
        }


        private static void ReceiveError(object sender, ExceptionEventArgs e)
        {
            Console.WriteLine("Error during reception of packet: {0}", e.Exception.Message);
        }

    }
}