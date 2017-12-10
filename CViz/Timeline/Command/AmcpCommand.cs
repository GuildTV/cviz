using System;
using System.Text;
using StilSoft.CasparCG.AmcpClient.Commands;
using StilSoft.CasparCG.AmcpClient.Common;

namespace CViz.Timeline.Command
{
    class AmcpCommand : CommandBase
    {
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
            foreach (string param in parameters.Split(' '))
            {
                string param2 = timeline.GetParameterValue(param, true);

                translatedParameters.Append(param2).Append(" ");
            }

            try
            {
                new CustomCommand($"{command} {LayerId} {translatedParameters}").Execute(timeline.Client);
                return true;
            }
            catch (Exception e)
            {
                Console.WriteLine("Failed to execute command: " + e.Message);
                return false;
            }
        }

        protected virtual string CommandName => "AmcpCommand";

        public override string ToString()
        {
            return string.Format("{0}: {1} {2}", CommandName, LayerId, _command);
        }
    }
}
