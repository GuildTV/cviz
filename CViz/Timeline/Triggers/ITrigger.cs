using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    interface ITrigger
    {
        int Layer { get; }
        ImmutableList<CommandBase> Commands { get; }

        ITrigger WithCommand(CommandBase cmd);

        ITrigger Clone();
    }
}