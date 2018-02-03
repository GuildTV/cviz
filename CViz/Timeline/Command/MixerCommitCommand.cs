using System;
using log4net;

namespace CViz.Timeline.Command
{
    class MixerCommitCommand : CommandBase
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(MixerCommitCommand));

        public MixerCommitCommand(int layerId) : base(0)
        {
            if (layerId != 0)
                throw new ArgumentException("Must be layer 0", nameof(layerId));
        }

        public override bool Execute(ITimeline timeline)
        {
            try
            {
                new StilSoft.CasparCG.AmcpClient.Commands.Mixer.MixerCommitCommand(timeline.ChannelNumber).Execute(timeline.CasparClient);
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
            return "MixerCommitCommand";
        }
    }
}