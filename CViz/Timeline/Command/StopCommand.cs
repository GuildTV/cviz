namespace CViz.Timeline.Command
{
    class StopCommand : AmcpCommand
    {
        public StopCommand(int layerId, string command) : base(layerId, command)
        {
        }

        public override bool Execute(ITimeline timeline)
        {
            timeline.RemoveAllTriggers(t => t.Loop && t.LayerId == LayerId);

            return base.Execute(timeline);
        }

        protected override string CommandName => "StopCommand";
    }
}