package cviz.control.tcp;

import cviz.TimelineManager;
import cviz.TimelineState;

import java.io.Serializable;

public class TCPControlState implements Serializable {
    private static final long serialVersionUID = 8408870393313567760L;

    private final transient TimelineManager manager;

    private TimelineState state;
    private String templateName;
    private String dataId;

    public TCPControlState(TimelineManager manager){
        this.manager = manager;

        state = TimelineState.ERROR;
    }

    public TimelineState getState(){
        return state;
    }

    public void setState(TimelineState state){
        switch(state){
            case CLEAR:
            case ERROR:
                templateName = dataId = "";
        }
        this.state = state;
    }

    public void runAction(ClientAction action){

        switch(action.getType()){
            case KILL:
                manager.killTimeline(action.getChannel());
                break;

            case LOAD:
                if(manager.loadTimeline(action.getChannel(), action.getFilename())) {
                    templateName = action.getFilename() != null ? action.getFilename() : "";
                    dataId = action.getTemplateDataId() != null ? action.getTemplateDataId() : "";

                    manager.startTimeline(action.getChannel(), action.getTemplateData());
                }
                break;

            case CUE:
                manager.triggerCue(action.getChannel());
                break;

            case QUERY:
                // Nothing to do
                break;

            default:
                System.err.println("Unknown action type: "+action.getType());
                break;
        }
    }

    public String toString(){
        return state + " " + templateName + " " + dataId;
    }

}
