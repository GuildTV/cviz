package cviz.timeline.commands;

import cviz.ITimeline;
import cviz.LayerState;

public class LoadCommand extends AmcpCommand {
    private String filename;

    public LoadCommand(int layerId, String command, String filename) {
        super(layerId, command);
        this.filename = filename;
    }

    @Override
    public boolean execute(ITimeline timeline) {
        if (!super.execute(timeline))
            return false;

        timeline.setLayerState(getLayerId(), new LayerState(filename));
        return true;
    }

    @Override
    public String getCommandName(){
        return "LoadCommand";
    }
}
