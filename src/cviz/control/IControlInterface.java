package cviz.control;

public interface IControlInterface extends Runnable {
    void setWaitingForCue();

    void setWaitingForTimeline();
    void setTimelineLoaded();
}
