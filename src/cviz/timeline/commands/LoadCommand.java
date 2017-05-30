package cviz.timeline.commands;

import cviz.ITimeline;
import cviz.LayerState;

public class LoadCommand extends AmcpCommand {
    private String filename;

    public LoadCommand(int layerId, String command, String filename) {
        super(layerId, command);
        this.filename = filename;
    }

    public String[] getParameters() {
        if (filename.indexOf("@") != 0)
            return new String[0];

        return new String[]{filename};
    }

    @Override
    public boolean execute(ITimeline timeline) {
        if (!super.execute(timeline))
            return false;

        String resolvedFilename = timeline.getParameterValue(filename, false);
        if (resolvedFilename == null)
            resolvedFilename = filename;

        timeline.setLayerState(getLayerId(), new LayerState(resolvedFilename));
        return true;
    }

    @Override
    public String getCommandName(){
        return "LoadCommand";
    }
}
