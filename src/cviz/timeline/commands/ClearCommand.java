package cviz.timeline.commands;

import cviz.IProcessor;
import se.svt.caspar.amcp.AmcpLayer;

public class ClearCommand extends ICommand {
    public ClearCommand(int layerId) {
        super(layerId);
    }

    @Override
    public boolean execute(IProcessor processor) {
        AmcpLayer layer = processor.getLayer(getLayerId());

        try {
            layer.clear();
            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "ClearCommand: " + getLayerId();
    }
}
