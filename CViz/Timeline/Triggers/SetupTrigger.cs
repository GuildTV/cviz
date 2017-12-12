using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    class SetupTrigger : ITrigger
    {
        public int Layer => 0;
        public ImmutableList<CommandBase> Commands { get; }

        public SetupTrigger(ImmutableList<CommandBase> commands = null)
        {
            Commands = commands ?? ImmutableList<CommandBase>.Empty;
        }

        public ITrigger WithCommand(CommandBase cmd)
        {
            return new SetupTrigger(Commands.Add(cmd));
        }

        public override string ToString()
        {
            return "SetupTrigger";
        }
    }
}
