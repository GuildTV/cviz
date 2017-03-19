package cviz.control.tcp;

import java.io.Serializable;
import java.util.HashMap;

public class ClientAction implements Serializable {
    private static final long serialVersionUID = 8403870393123567760L;

    public enum ActionType {
        LOAD,
        CUE,
        KILL,
        QUERY
    }

    private String channel;
    private String timelineSlot;
    private ActionType type;
    private String timelineFile;
    private String instanceName;
    private HashMap<String, String> parameters;

    public ClientAction(String channel, String timelineSlot, ActionType type, String timelineFile, String instanceName, HashMap<String, String> parameters) {
        this.channel = channel;
        this.timelineSlot = timelineSlot;
        this.type = type;
        this.timelineFile = timelineFile;
        this.instanceName = instanceName;
        this.parameters = parameters;
    }

    public String getChannel() {
        if (channel == null || channel.length() == 0)
            return "default";

        return channel;
    }
    public String getTimelineSlot() {
        if (timelineSlot == null || timelineSlot.length() == 0)
            return "default";

        return timelineSlot;
    }

    public ActionType getType() {
        return type;
    }

    public String getTimelineFile() {
        return timelineFile;
    }

    public String getInstanceId() {
        return instanceName;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public String toString() {
        return "Action: " + type + " " + timelineFile + " data: " + instanceName + " " + (parameters != null ? parameters.size() : "-");
    }
}
