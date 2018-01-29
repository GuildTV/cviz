namespace CViz
{
    enum LayerType
    {
        Video,
        Scene,
    }

    class LayerState
    {
        public string Name { get; }
        public long LastFrame { get; set; }
        public LayerType Type { get; }

        public LayerState(LayerType type, string name)
        {
            Type = type;
            Name = name;
            LastFrame = 0;
        }

        public override string ToString()
        {
            return $"LayerState: {Type} {Name} {LastFrame}";
        }
    }
}
