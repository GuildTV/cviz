using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    class LoopTrigger : EndTrigger
    {
        private static ImmutableList<CommandBase>  GetCommands(int layer, ImmutableList<CommandBase> cmds)
        {
            cmds = cmds ?? ImmutableList<CommandBase>.Empty;
            return cmds.Add(new LoopCommand(layer));
        }

        public LoopTrigger(int layer, ImmutableList<CommandBase> commands = null): base(layer, GetCommands(layer, commands))
        {
        }

        public override ITrigger WithCommand(CommandBase cmd)
        {
            return new LoopTrigger(Layer, Commands.Add(cmd));
        }

        public override string ToString()
        {
            return $"LoopTrigger: {Layer}";
        }

        public override ITrigger Clone()
        {
            return new LoopTrigger(Layer, Commands);
        }
    }
}