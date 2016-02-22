package cviz.timeline.commands;

import cviz.ITimeline;
import se.svt.caspar.amcp.AmcpLayer;

public class OpacityCommand extends ICommand {
    private final String parameters;

    public OpacityCommand(int layerId, String[] parameters) {
        super(layerId);
        this.parameters = String.join(" ", parameters);
    }

    @Override
    public boolean execute(ITimeline timeline) {
        AmcpLayer layer = timeline.getLayer(getLayerId());

        try {
            layer.sendCommand("MIXER", "OPACITY " + parameters);

            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "CgAddCommand: " + getLayerId() + " " + parameters;
    }
}
