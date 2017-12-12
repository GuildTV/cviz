using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    class LoopTrigger : ITrigger
    {
        public int Layer { get; }
        public ImmutableList<CommandBase> Commands { get; }

        public LoopTrigger(int layer, ImmutableList<CommandBase> commands = null)
        {
            Layer = layer;
            Commands = commands ?? ImmutableList<CommandBase>.Empty;
        }

        public ITrigger WithCommand(CommandBase cmd)
        {
            return new LoopTrigger(Layer, Commands.Add(cmd));
        }

        public override string ToString()
        {
            return $"LoopTrigger: {Layer}";
        }
    }
}