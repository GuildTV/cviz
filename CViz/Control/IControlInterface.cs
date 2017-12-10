using CViz.Timeline;

namespace CViz.Control
{
    interface IControlInterface
    {
        // It is important that calls to this do not block, as that will affect the main timeline thread
        void NotifyState(TimelineState state);

        void Run();
    }
}
