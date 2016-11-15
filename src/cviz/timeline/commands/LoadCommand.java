package cviz.timeline.commands;

import cviz.ITimeline;
import cviz.LayerState;
import se.svt.caspar.amcp.AmcpLayer;
import se.svt.caspar.producer.Video;

public class LoadCommand extends ICommand {
    private String filename;

    public LoadCommand(int layerId, String filename) {
        super(layerId);
        this.filename = filename;
    }

    public String[] getTemplateFields() {
        if (filename.indexOf("@") != 0)
            return new String[0];

        return new String[]{filename};
    }


    @Override
    public boolean execute(ITimeline timeline) {
        AmcpLayer layer = timeline.getLayer(getLayerId());

        try {
            String resolvedFilename = timeline.getParameter(filename);
            if (resolvedFilename == null)
                resolvedFilename = filename;

            layer.loadBg(new Video(resolvedFilename));
            timeline.setLayerState(getLayerId(), new LayerState(resolvedFilename));
            return true;
        } catch (Exception e) {
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "LoadCommand: " + getLayerId() + " " + filename;
    }
}
