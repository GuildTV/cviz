using CViz.Timeline.Triggers;

namespace CViz.Timeline.Command
{
    class StopCommand : AmcpCommand
    {
        public StopCommand(int layerId, string command) : base(layerId, command)
        {
        }

        public override bool Execute(ITimeline timeline)
        {
            timeline.RemoveAllTriggers(t => t is LoopTrigger && t.Layer == LayerId);

            return base.Execute(timeline);
        }

        protected override string CommandName => "StopCommand";
    }
}