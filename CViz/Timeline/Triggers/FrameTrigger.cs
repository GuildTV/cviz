using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    class FrameTrigger : ITrigger
    {
        public int Layer { get; }
        public ImmutableList<CommandBase> Commands { get; }

        public long TargetFrame { get; }
        public bool Waited { get; private set; }

        public FrameTrigger(int layer, long targetFrame, ImmutableList<CommandBase> commands = null)
        {
            Layer = layer;
            TargetFrame = targetFrame;
            Commands = commands ?? ImmutableList<CommandBase>.Empty;
        }

        public virtual ITrigger WithCommand(CommandBase cmd)
        {
            return new FrameTrigger(Layer, TargetFrame, Commands.Add(cmd));
        }

        public void SetWaited()
        {
            Waited = true;
        }

        public override string ToString()
        {
            return $"FrameTrigger: {Layer} F{TargetFrame}";
        }

        public virtual ITrigger Clone()
        {
            return new FrameTrigger(Layer, TargetFrame, Commands);
        }
    }
}