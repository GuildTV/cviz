using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    class EndTrigger : FrameTrigger
    {
        public EndTrigger(int layer, ImmutableList<CommandBase> commands = null) : base(layer, -1, commands)
        {
        }

        public override ITrigger WithCommand(CommandBase cmd)
        {
            return new EndTrigger(Layer, Commands.Add(cmd));
        }

        public override string ToString()
        {
            return $"EndTrigger: {Layer}";
        }

        public override ITrigger Clone()
        {
            return new EndTrigger(Layer, Commands);
        }
    }
}