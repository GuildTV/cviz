using System;
using CViz.Control;

namespace CViz.State
{
    class State
    {
        private readonly IControlInterface _controlInterface;

        public State(IControlInterface controlInterface, string timelineSlot, string timelineFile, string instanceName)
        {
            _controlInterface = controlInterface;
            TimelineSlot = timelineSlot;
            TimelineFile = timelineFile;
            InstanceName = instanceName;

            TimelineState = TimelineState.Ready;
            StateMessage = null;
        }

        public string TimelineSlot { get; }
        public string TimelineFile { get; }
        public string InstanceName { get; }

        public TimelineState TimelineState { get; private set; }
        public string StateMessage { get; private set; }

        public void SetState(TimelineState state, string message = null)
        {
            TimelineState = state;
            StateMessage = message;

            _controlInterface.NotifyState(this);
        }
    }
}
