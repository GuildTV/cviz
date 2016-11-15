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
    private String name;
    private ActionType type;
    private String filename;
    private String templateDataId;
    private HashMap<String, String> templateData;

    public ClientAction(String channel, String name, ActionType type, String filename, String templateDataId, HashMap<String, String> templateData) {
        this.channel = channel;
        this.name = name;
        this.type = type;
        this.filename = filename;
        this.templateDataId = templateDataId;
        this.templateData = templateData;
    }

    public String getChannel() {
        if (channel == null || channel.length() == 0)
            return "default";

        return channel;
    }
    public String getName() {
        if (name == null || name.length() == 0)
            return "default";

        return name;
    }

    public ActionType getType() {
        return type;
    }

    public String getFilename() {
        return filename;
    }

    public String getTemplateDataId() {
        return templateDataId;
    }

    public HashMap<String, String> getTemplateData() {
        return templateData;
    }

    public String toString() {
        return "Action: " + type + " " + filename + " data: " + templateDataId + " " + (templateData != null ? templateData.size() : "-");
    }
}
