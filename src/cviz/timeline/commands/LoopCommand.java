package cviz.timeline.commands;

import cviz.ITimeline;
import cviz.LayerState;
import cviz.timeline.Trigger;
import se.svt.caspar.amcp.AmcpLayer;
import se.svt.caspar.producer.Video;

public class LoopCommand extends ICommand {
    public LoopCommand(int layerId) {
        super(layerId);
    }

    @Override
    public boolean execute(ITimeline timeline) {
        AmcpLayer layer = timeline.getLayer(getLayerId());

        try {
            layer.play();
            LayerState state = timeline.getLayerState(getLayerId());
            if(state == null)
                throw new Exception("Missing layer state for loop " + getLayerId());

            layer.loadBg(new Video(state.getVideoName()));

            Trigger t = Trigger.CreateLoop(getLayerId());
            timeline.getActiveTriggers().add(t);

            System.out.println("Looping: " + state.toString());

            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "LoopCommand: " + getLayerId();
    }
}
