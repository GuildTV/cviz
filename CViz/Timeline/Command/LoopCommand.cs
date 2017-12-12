using System;
using CViz.Timeline.Triggers;
using log4net;
using StilSoft.CasparCG.AmcpClient.Commands.Basic;

namespace CViz.Timeline.Command
{
    class LoopCommand : CommandBase
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(LoopCommand));

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
                timeline.AddTrigger(new LoopTrigger(LayerId));
                
                Log.InfoFormat("Looping: {0}", state);

                return true;
            }
            catch (Exception e)
            {
                Log.ErrorFormat("Failed to execute command: {0}", e.Message);
                return false;
            }
        }

        public override string ToString()
        {
            return $"LoopCommand: {LayerId}";
        }
    }
}