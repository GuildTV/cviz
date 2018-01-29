using System.Collections.Immutable;
using CViz.Timeline.Command;

namespace CViz.Timeline.Triggers
{
    class SceneStopTrigger : ITrigger
    {
        public int Layer { get; }
        public string StopName { get; }
        public ImmutableList<CommandBase> Commands { get; }
        
        public SceneStopTrigger(int layer, string stopName, ImmutableList<CommandBase> commands = null)
        {
            Layer = layer;
            StopName = stopName;
            Commands = commands ?? ImmutableList<CommandBase>.Empty;
        }

        public ITrigger WithCommand(CommandBase cmd)
        {
            return new SceneStopTrigger(Layer, StopName, Commands.Add(cmd));
        }

        public override string ToString()
        {
            return $"SceneStopTrigger: {Layer} {StopName}";
        }

        public ITrigger Clone()
        {
            return new SceneStopTrigger(Layer, StopName, Commands);
        }
    }
}