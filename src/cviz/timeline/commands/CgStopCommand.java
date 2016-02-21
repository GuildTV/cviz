package cviz.timeline.commands;

import cviz.ITimeline;
import se.svt.caspar.amcp.AmcpLayer;

public class CgStopCommand extends ICommand {
    public CgStopCommand(int layerId) {
        super(layerId);
    }

    @Override
    public boolean execute(ITimeline timeline) {
        AmcpLayer layer = timeline.getLayer(getLayerId());

        try {
            layer.sendCommand("CG", "STOP 1");
            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "CgStopCommand: " + getLayerId();
    }
}
