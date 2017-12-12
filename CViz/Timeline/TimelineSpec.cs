using System.Collections.Generic;
using System.Collections.Immutable;
using CViz.Timeline.Triggers;

namespace CViz.Timeline
{
    class TimelineSpec
    {
        public string Name { get; }
        public ImmutableArray<ITrigger> Triggers { get; }
        public ImmutableDictionary<string, ImmutableArray<ITrigger>> ChildTimelines { get; }

        public TimelineSpec(string name, IEnumerable<ITrigger> triggers, Dictionary<string, IEnumerable<ITrigger>> childTimelines)
        {
            Name = name;
            ChildTimelines = childTimelines.ToImmutableDictionary(k => k.Key, k => k.Value.ToImmutableArray());
            Triggers = triggers.ToImmutableArray();
        }
    }
}
