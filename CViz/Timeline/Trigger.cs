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
        public static Trigger CreateEnd(int layerId) => new Trigger(TriggerType.End, string.Format("{0} End", layerId));

        public static Trigger CreateFrame(int layerId, long targetFrame)
        {
            return new Trigger(TriggerType.Frame, string.Format("{0} F{1}", layerId, targetFrame), layerId, targetFrame);
        }

        public static Trigger CreateLoop(int layerId)
        {
            Trigger t = new Trigger(TriggerType.End, string.Format("{0} Loop", layerId), layerId, loop: true);
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
            return string.Format("Trigger: ({0}) {1} {2} {3} Loop:{4}", Name, Type, LayerId, TargetFrame, Loop);
        }
    }
}
