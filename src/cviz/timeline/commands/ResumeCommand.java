package cviz.timeline.commands;

import cviz.ITimeline;
import se.svt.caspar.amcp.AmcpLayer;

public class ResumeCommand extends ICommand {
    public ResumeCommand(int layerId) {
        super(layerId);
    }

    @Override
    public boolean execute(ITimeline timeline) {
        AmcpLayer layer = timeline.getLayer(getLayerId());

        try {
            layer.sendCommand("RESUME");
            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "ResumeCommand: " + getLayerId();
    }
}
