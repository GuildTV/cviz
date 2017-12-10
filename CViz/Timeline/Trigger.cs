using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline
{
    class Trigger
    {
        public TriggerType Type { get; }
        public string Name { get; }
        public int LayerId { get; }
        public long TargetFrame { get; }
        public bool Loop { get; }
        public bool Waited { get; private set; }

        public ImmutableList<CommandBase> Commands { get; }

        public static Trigger CreateCue(string name) => new Trigger(TriggerType.Cue, name);
        public static Trigger CreateSetup() => new Trigger(TriggerType.Setup, "Setup");
        public static Trigger CreateEnd(int layerId) => new Trigger(TriggerType.End, $"{layerId} End");

        public static Trigger CreateFrame(int layerId, long targetFrame)
        {
            return new Trigger(TriggerType.Frame, $"{layerId} F{targetFrame}", layerId, targetFrame);
        }

        public static Trigger CreateLoop(int layerId)
        {
            Trigger t = new Trigger(TriggerType.End, $"{layerId} Loop", layerId, loop: true);
            return t.WithCommand(new LoopCommand(layerId));
        }

        private Trigger(TriggerType type, string name, int layerId = -1, long targetFrame = -1, bool loop = false, ImmutableList<CommandBase> commands=null)
        {
            Type = type;
            Name = name;
            LayerId = layerId;
            TargetFrame = targetFrame;
            Loop = loop;

            Commands = commands ?? ImmutableList<CommandBase>.Empty;
        }

        public Trigger WithCommand(CommandBase cmd)
        {
            ImmutableList<CommandBase> cmds = Commands.Add(cmd);
            return new Trigger(Type, Name, LayerId, TargetFrame, Loop, cmds);
        }

        public void SetWaited()
        {
            Waited = true;
        }

        public override string ToString()
        {
            return $"Trigger: ({Name}) {Type} {LayerId} {TargetFrame} Loop:{Loop}";
        }
    }
}
