package cviz.timeline.commands;

import cviz.ITimeline;
import se.svt.caspar.amcp.AmcpLayer;

public class TransformCommand extends ICommand {
    private final String parameters;

    public TransformCommand(int layerId, String[] parameters) {
        super(layerId);
        this.parameters = String.join(" ", parameters);
    }

    @Override
    public boolean execute(ITimeline timeline) {
        AmcpLayer layer = timeline.getLayer(getLayerId());

        try {
            //layer.fill().modify(v0, v1, v2, v3);
            layer.sendCommand("MIXER", "FILL " + parameters);

            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "TransformCommand: " + getLayerId() + " " + parameters;
    }
}
