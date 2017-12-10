namespace CViz.Timeline.Command
{
    class LoadCommand : AmcpCommand
    {
        private readonly string _filename;

        public LoadCommand(int layerId, string command, string filename) : base(layerId, command)
        {
            _filename = filename;
        }

        public override bool Execute(ITimeline timeline)
        {
            if (!base.Execute(timeline))
                return false;

            string resolvedFilename = timeline.GetParameterValue(_filename, false) ?? _filename;
            timeline.SetLayerState(LayerId, new LayerState(resolvedFilename));
            return true;
        }

        protected override string CommandName => "LoadCommand";

        public override string[] Parameters
        {
            get
            {
                if (_filename.IndexOf("@") != 0)
                    return new string[0];

                return new[] { _filename };
            }
        }
    }
}