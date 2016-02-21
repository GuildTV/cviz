package cviz.control;

import cviz.TimelineState;

public interface IControlInterface extends Runnable {
    // It is important that calls to this do not block, as that will affect the main timeline thread
    void notifyState(TimelineState state);
}
