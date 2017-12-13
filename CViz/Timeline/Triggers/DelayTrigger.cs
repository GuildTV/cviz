using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    class DelayTrigger : ITrigger
    {
        public int Layer => 0;
        public ImmutableList<CommandBase> Commands { get; }

        public int Duration { get; }
        public bool DelayStarted { get; private set; }
        public long DelayStartedAt { get; private set; }
        public long DelayEndAt => DelayStartedAt + Duration;

        public DelayTrigger(int duration, ImmutableList<CommandBase> commands = null)
        {
            Duration = duration;
            Commands = commands ?? ImmutableList<CommandBase>.Empty;
        }

        public ITrigger WithCommand(CommandBase cmd)
        {
            return new DelayTrigger(Duration, Commands.Add(cmd));
        }

        public void StartDelay(long startFrame)
        {
            if (DelayStarted)
                return;

            DelayStarted = true;
            DelayStartedAt = startFrame;
        }

        public override string ToString()
        {
            return $"DelayTrigger: {Duration}";
        }

        public ITrigger Clone()
        {
            return new DelayTrigger(Duration, Commands);
        }
    }
}