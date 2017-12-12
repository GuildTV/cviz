namespace CViz.Timeline.Command
{
    class PlayCommand : AmcpCommand
    {
        public PlayCommand(int layerId, string command) : base(layerId, command)
        {
        }

        public override bool Execute(ITimeline timeline)
        {
            if (!base.Execute(timeline))
                return false;

            LayerState state = timeline.GetLayerState(LayerId);
            if (state != null)
                state.LastFrame = 0;

            return true;
        }

        protected override string CommandName => "PlayCommand";
    }
}