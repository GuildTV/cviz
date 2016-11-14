package cviz.timeline.commands;

import cviz.ITimeline;

public abstract class ICommand {
    private final int layerId;

    protected ICommand(int layerId){
        this.layerId = layerId;
    }

    public int getLayerId(){
        return layerId;
    }

    public String[] getTemplateFields(){
        return new String[0];
    }

    public abstract boolean execute(ITimeline timeline);

    public abstract String toString();
}
