using CViz.Control;

namespace CViz.Timeline
{
    class TimelineState
    {
        public enum StateType
        {
            Error,
            Ready,
            Cue,
            CueOrChild,
            Run,
            Clear,
        }

        private readonly IControlInterface _controlInterface;

        public TimelineState(IControlInterface controlInterface, string timelineSlot, string timelineFile, string instanceName)
        {
            _controlInterface = controlInterface;
            TimelineSlot = timelineSlot;
            TimelineFile = timelineFile;
            InstanceName = instanceName;

            State = StateType.Ready;
            StateMessage = null;
        }

        public string TimelineSlot { get; }
        public string TimelineFile { get; }
        public string InstanceName { get; set; }

        public StateType State { get; private set; }
        public string StateMessage { get; private set; }

        public void SetState(StateType state, string message = null)
        {
            State = state;
            StateMessage = message;

            _controlInterface?.NotifyState(this);
        }
    }
}
