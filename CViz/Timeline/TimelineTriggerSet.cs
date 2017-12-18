using System.Collections.Generic;
using System.Collections.Immutable;
using CViz.Timeline.Triggers;

namespace CViz.Timeline
{
    class TimelineTriggerSet
    {
        public List<ITrigger> Remaining { get; }
        public List<ITrigger> Active { get; }

        public ImmutableDictionary<string, string> ParameterValues { get; private set; }

        public TimelineTriggerSet(List<ITrigger> remaining, List<ITrigger> active, ImmutableDictionary<string, string> parameterValues = null)
        {
            Remaining = remaining;
            Active = active;
            ParameterValues = parameterValues ?? ImmutableDictionary<string, string>.Empty;
        }

        public void SetParameters(ImmutableDictionary<string, string> parameters)
        {
            if (ParameterValues != ImmutableDictionary<string, string>.Empty)
                return;

            ParameterValues = parameters;
        }
    }
}