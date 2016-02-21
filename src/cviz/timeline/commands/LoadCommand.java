package cviz.timeline.commands;

import cviz.IProcessor;
import cviz.LayerState;
import se.svt.caspar.amcp.AmcpLayer;
import se.svt.caspar.producer.Video;

public class LoadCommand extends ICommand {
    private String filename;

    public LoadCommand(int layerId, String filename) {
        super(layerId);
        this.filename = filename;
    }

    @Override
    public boolean execute(IProcessor processor) {
        AmcpLayer layer = processor.getLayer(getLayerId());

        try {
            layer.loadBg(new Video(filename));
            processor.setLayerState(getLayerId(), new LayerState(filename));
            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "LoadCommand: " + getLayerId() + " " + filename;
    }
}
