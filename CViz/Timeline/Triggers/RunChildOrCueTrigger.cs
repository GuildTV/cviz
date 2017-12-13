using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    class RunChildOrCueTrigger : CueTrigger
    {
        public string TimelineName { get; }

        public RunChildOrCueTrigger(string timelineName, string name, ImmutableList<CommandBase> commands = null) : base(name, commands)
        {
            TimelineName = timelineName;
        }

        public override ITrigger WithCommand(CommandBase cmd)
        {
            return new RunChildOrCueTrigger(TimelineName, Name, Commands.Add(cmd));
        }

        public override string ToString()
        {
            return $"RunChildOrCueTrigger: {Name} Child: {TimelineName}";
        }

        public override ITrigger Clone()
        {
            return new RunChildOrCueTrigger(TimelineName, Name, Commands);
        }
    }
}