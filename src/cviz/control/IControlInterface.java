package cviz.control;

import cviz.TimelineState;

public interface IControlInterface extends Runnable {
    void notifyState(TimelineState state);
}
