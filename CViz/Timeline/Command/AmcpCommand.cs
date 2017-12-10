using System;
using System.Linq;
using System.Text;
using log4net;
using StilSoft.CasparCG.AmcpClient.Commands;

namespace CViz.Timeline.Command
{
    class AmcpCommand : CommandBase
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(AmcpCommand));

        private readonly string _command;

        public AmcpCommand(int layerId, string command) : base(layerId)
        {
            _command = command;
        }

        public override bool Execute(ITimeline timeline)
        {
            string[] parts = _command.Split(new []{' '}, 2);

            return SendCommand(timeline, parts[0], parts.Length > 1 ? parts[1] : "");
        }

        private bool SendCommand(ITimeline timeline, string command, string parameters)
        {
            var translatedParameters = new StringBuilder();
            foreach (string param in parameters.Split(' ').Where(p => p.Length > 0))
            {
                string param2 = timeline.GetParameterValue(param, true);

                translatedParameters.Append("\"").Append(param2).Append("\" ");
            }

            try
            {
                new CustomCommand($"{command} {timeline.ChannelNumber}-{LayerId} {translatedParameters}").Execute(timeline.Client);
                return true;
            }
            catch (Exception e)
            {
                Log.ErrorFormat("Failed to execute command: {0}", e.Message);
                return false;
            }
        }

        protected virtual string CommandName => "AmcpCommand";

        public override string ToString()
        {
            return $"{CommandName}: {LayerId} {_command}";
        }
    }
}
