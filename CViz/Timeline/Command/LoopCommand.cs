using System;
using StilSoft.CasparCG.AmcpClient.Commands.Basic;

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
                new PlayCommand(timeline.ChannelNumber, LayerId).Execute(timeline.Client);
                LayerState state = timeline.GetLayerState(LayerId);
                if (state == null)
                    throw new Exception("Missing layer state for loop " + LayerId);

                new LoadBgCommand(timeline.ChannelNumber, LayerId, state.VideoName).Execute(timeline.Client);
                timeline.AddTrigger(Trigger.CreateLoop(LayerId));

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