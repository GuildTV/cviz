using System.Collections.Generic;
using System.Linq;

namespace CViz.Timeline.Command
{
    class StopCommand : AmcpCommand
    {
        public StopCommand(int layerId, string command) : base(layerId, command)
        {
        }

        public override bool Execute(ITimeline timeline)
        {
            List<Trigger> oldTriggers = timeline.ActiveTriggers.Where(t => t.Loop && t.LayerId == LayerId).ToList();
            timeline.ActiveTriggers.RemoveAll(oldTriggers);

            return base.Execute(timeline);
        }

        protected override string CommandName => "StopCommand";
    }
}