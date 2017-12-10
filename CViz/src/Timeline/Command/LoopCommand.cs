using System;
using System.Threading.Tasks;
using StilSoft.CasparCG.AmcpClient.Commands.Basic;
using StilSoft.CasparCG.AmcpClient.Common;

namespace CViz.Timeline.Command
{
    class LoopCommand : CommandBase
    {
        public LoopCommand(int layerId) : base(layerId)
        {
        }

        public override bool Execute(ITimeline timeline)
        {
            try
            {
                Task<AmcpResponse> task = new PlayCommand(timeline.ChannelNumber, LayerId).ExecuteAsync(timeline.Client);
                LayerState state = timeline.GetLayerState(LayerId);
                if (state == null)
                    throw new Exception("Missing layer state for loop " + LayerId);

                task.ContinueWith(r =>
                {
                    new LoadBgCommand(timeline.ChannelNumber, LayerId, state.VideoName).ExecuteAsync(timeline.Client);
                });

                Trigger t = Trigger.CreateLoop(LayerId);
                timeline.ActiveTriggers.Add(t);

                Console.WriteLine("Looping: " + state);

                return true;
            }
            catch (Exception e)
            {
                Console.WriteLine("Failed to execute command: " + e.Message);
                return false;
            }
        }

        public override string ToString()
        {
            return string.Format("LoopCommand: {0}", LayerId);
        }
    }
}