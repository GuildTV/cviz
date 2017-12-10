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
            return string.Format("LayerState: {0} {1}", VideoName, LastFrame);
        }
    }
}
