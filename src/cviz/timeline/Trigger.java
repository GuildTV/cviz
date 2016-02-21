package cviz.timeline;

import cviz.timeline.commands.ICommand;
import cviz.timeline.commands.LoopCommand;

import java.util.LinkedList;
import java.util.List;

public class Trigger {
	private final TriggerType type;
	private final long targetFrame;
	private final int layerId;
	private final boolean loop;
	private boolean waited = false;
	
	private final LinkedList<ICommand> commands = new LinkedList<>();

	public static Trigger CreateCue(){
		return new Trigger(TriggerType.CUE, -1, -1, false);
	}
	public static Trigger CreateImmediate(){
		return new Trigger(TriggerType.IMMEDIATE, -1, -1, false);
	}
	public static Trigger CreateFrame(int layerId, long targetFrame){
		return new Trigger(TriggerType.FRAME, layerId, targetFrame, false);
	}
	public static Trigger CreateEnd(int layerId){
		return new Trigger(TriggerType.END, layerId, -1, false);
	}
	public static Trigger CreateLoop(int layerId){
		Trigger t = new Trigger(TriggerType.END, layerId, -1, true);
		t.addCommand(new LoopCommand(layerId));
		return t;
	}

	private Trigger(TriggerType type, int layerId, long targetFrame, boolean loop) {
		this.type = type;
		this.targetFrame = targetFrame;
		this.layerId = layerId;
		this.loop = loop;
	}
	
	protected void addCommand(ICommand c) {
		commands.add(c);
	}

    public List<ICommand> getCommands(){
        return commands;
    }

	public TriggerType getType() {
		return type;
	}
	
	public long getTargetFrame() {
		return targetFrame;
	}
	
	public int getLayerId() {
		return layerId;
	}
	
	public void setWaited() {
		waited = true;
	}
	
	public boolean hasWaited() {
		return waited;
	}
	
	public boolean isLoop() {
		return loop;
	}

	public String toString() {
		return "Trigger: " + type + " " + layerId + " " + targetFrame + " loop: " + loop;
	}
}
