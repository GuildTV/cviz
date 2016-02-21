package cviz.timeline.commands;

import cviz.IProcessor;

public abstract class ICommand {
    private final int layerId;

    protected ICommand(int layerId){
        this.layerId = layerId;
    }

    public int getLayerId(){
        return layerId;
    }

    public abstract boolean execute(IProcessor processor);

    public abstract String toString();
}
