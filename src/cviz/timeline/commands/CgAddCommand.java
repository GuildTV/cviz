package cviz.timeline.commands;

import cviz.IProcessor;
import se.svt.caspar.amcp.AmcpLayer;

public class CgAddCommand extends ICommand {
    private final String templateName;
    private final String templateField;

    public CgAddCommand(int layerId, String templateName, String templateField) {
        super(layerId);
        this.templateName = templateName;
        this.templateField = templateField;
    }

    @Override
    public boolean execute(IProcessor processor) {
        AmcpLayer layer = processor.getLayer(getLayerId());

        try {
            String templateData = processor.getTemplateData(templateField);
            layer.sendCommand("CG", "ADD 1 " + templateName + " 0 " + templateData);

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
