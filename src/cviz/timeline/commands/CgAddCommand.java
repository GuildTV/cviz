package cviz.timeline.commands;

import cviz.ITimeline;
import se.svt.caspar.amcp.AmcpLayer;

public class CgAddCommand extends ICommand {
    private final String templateName;
    private final String templateField;

    public CgAddCommand(int layerId, String templateName, String templateField) {
        super(layerId);
        this.templateName = templateName;
        this.templateField = templateField;
    }

    public String[] getTemplateFields() {
        if (templateField.indexOf("@") != 0)
            return new String[0];

        return new String[]{templateField};
    }

    @Override
    public boolean execute(ITimeline timeline) {
        AmcpLayer layer = timeline.getLayer(getLayerId());

        try {
            String templateData = timeline.getTemplateData(templateField);

            // if we couldnt match the data, assume it was passed in direct from the timeline file
            if(templateData == null)
                templateData = templateField;

            templateData = templateData.replace("\\\"", "\\\\\\\\\""); // \" => \\\\"
            templateData = templateData.replace("\"", "\\\""); // " => \"

            layer.sendCommand("CG", "ADD 1 \"" + templateName + "\" 0 \"" + templateData + "\"");

            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "CgAddCommand: " + getLayerId() + " " + templateName + " " + templateField;
    }
}
