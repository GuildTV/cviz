namespace CViz
{
    class LayerState
    {
        public string VideoName { get; }
        public long LastFrame { get; set; }

        public LayerState(string videoName)
        {
            VideoName = videoName;
            LastFrame = 0;
        }

        public override string ToString()
        {
            return $"LayerState: {VideoName} {LastFrame}";
        }
    }
}
