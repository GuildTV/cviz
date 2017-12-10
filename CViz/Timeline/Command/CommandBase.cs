namespace CViz.Timeline.Command
{
    abstract class CommandBase
    {
        protected CommandBase(int layerId)
        {
            LayerId = layerId;
        }

        public int LayerId { get; }

        public abstract bool Execute(ITimeline timeline);

        public virtual string[] Parameters => new string[0];

        public abstract override string ToString();
    }
}