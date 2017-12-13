using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    class CueTrigger : ITrigger
    {
        public string Name { get; }
        public int Layer => 0;
        public ImmutableList<CommandBase> Commands { get; }

        public CueTrigger(string name, ImmutableList<CommandBase> commands = null)
        {
            Name = name;
            Commands = commands ?? ImmutableList<CommandBase>.Empty;
        }

        public virtual ITrigger WithCommand(CommandBase cmd)
        {
            return new CueTrigger(Name, Commands.Add(cmd));
        }

        public override string ToString()
        {
            return $"CueTrigger: {Name}";
        }

        public virtual ITrigger Clone()
        {
            return new CueTrigger(Name, Commands);
        }
    }
}