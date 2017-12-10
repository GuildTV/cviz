namespace CViz.Control
{
    interface IControlInterface
    {
        // It is important that calls to this do not block, as that will affect the main timeline thread
        void NotifyState(State.State state);
    }
}
