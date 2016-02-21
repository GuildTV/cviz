package cviz.control.tcp;

import java.io.Serializable;
import java.util.HashMap;

public class ClientAction implements Serializable {
    private static final long serialVersionUID = 8403870393123567760L;

    public enum ActionType{
        LOAD,
        CUE,
        KILL
    }

    private ActionType type;
    private String filename;
    private HashMap<String, String> templateData;

    public ClientAction(ActionType type, String filename, HashMap<String, String> templateData){
        this.type = type;
        this.filename = filename;
        this.templateData = templateData;
    }

    public ActionType getType(){
        return type;
    }

    public String getFilename(){
        return filename;
    }

    public HashMap<String, String> getTemplateData(){
        return templateData;
    }

    public String toString(){
        return "Action: " + type + " " + filename + " data: " + (templateData != null?templateData.size():"-");
    }
}
