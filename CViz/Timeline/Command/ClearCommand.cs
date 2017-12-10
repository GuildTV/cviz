using System;
using log4net;
using StilSoft.CasparCG.AmcpClient.Commands.Mixer;
using StilSoft.CasparCG.AmcpClient.Commands.Mixer.Common;

namespace CViz.Timeline.Command
{
    class ClearCommand : CommandBase
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(ClearCommand));

        public ClearCommand(int layerId) : base(layerId)
        {
        }

        public override bool Execute(ITimeline timeline)
        {
            try
            {
                new StilSoft.CasparCG.AmcpClient.Commands.Basic.ClearCommand(timeline.ChannelNumber, LayerId).ExecuteAsync(timeline.Client);
                // make sure to reset opacity and transforms, or we could fuck up the next timeline
                new MixerOpacitySetCommand(timeline.ChannelNumber, LayerId, 1).ExecuteAsync(timeline.Client);
                new MixerFillSetCommand(timeline.ChannelNumber, LayerId, new MixerFill(0, 0, 1, 1)).ExecuteAsync(timeline.Client);
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
            return $"ClearCommand: {LayerId}";
        }
    }
}