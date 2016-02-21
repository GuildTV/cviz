package cviz.timeline.commands;

import cviz.ITimeline;
import cviz.timeline.Trigger;
import se.svt.caspar.amcp.AmcpLayer;

import java.util.List;
import java.util.stream.Collectors;

public class StopCommand extends ICommand {
    public StopCommand(int layerId) {
        super(layerId);
    }

    @Override
    public boolean execute(ITimeline timeline) {
        AmcpLayer layer = timeline.getLayer(getLayerId());

        try {
            layer.stop();

            List<Trigger> oldTriggers = timeline.getActiveTriggers().stream()
                    .filter(t -> t.isLoop() && t.getLayerId() == getLayerId())
                    .collect(Collectors.toList());
            timeline.getActiveTriggers().removeAll(oldTriggers);
            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "StopCommand: " + getLayerId();
    }
}
