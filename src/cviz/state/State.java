package cviz.state;

import cviz.TimelineState;
import cviz.control.IControlInterface;

import java.io.Serializable;

public class State implements Serializable{
    private transient final IControlInterface controlInterface;

    private final String timelineSlot;
    private final String timelineFile;
    private final String instanceName;
    private TimelineState state;
    private String stateMessage;

    public State(IControlInterface controlInterface, String timelineSlot, String timelineFile, String instanceName){
        this.controlInterface = controlInterface;
        this.timelineSlot = timelineSlot;
        this.timelineFile = timelineFile;

        this.instanceName = instanceName;

        this.state = TimelineState.READY;
        this.stateMessage = null;
    }

    public void setState(TimelineState state){
        setState(state, null);
    }
    public void setState(TimelineState state, String stateMessage){
        this.state = state;
        this.stateMessage = stateMessage;

        controlInterface.notifyState(this);
    }

    public String getTimelineSlot() {
        return timelineSlot;
    }

    public String getTimelineFile() {
        return timelineFile;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public TimelineState getState() {
        return state;
    }

    public String getStateMessage() {
        return stateMessage;
    }
}
