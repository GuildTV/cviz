package cviz.timeline;

import cviz.timeline.commands.ICommand;
import cviz.timeline.commands.LoopCommand;

import java.util.LinkedList;
import java.util.List;

public class Trigger {
	private TriggerType type;
	private long targetFrame;
	private int layerId;
	private boolean loop = false;
	private boolean waited = false;
	
	private LinkedList<ICommand> commands = new LinkedList<>();

	public static Trigger CreateCue(){
		return new Trigger(TriggerType.CUE, -1, -1);
	}
	public static Trigger CreateImmediate(){
		return new Trigger(TriggerType.IMMEDIATE, -1, -1);
	}
	public static Trigger CreateFrame(int layerId, long targetFrame){
		return new Trigger(TriggerType.FRAME, layerId, targetFrame);
	}
	public static Trigger CreateEnd(int layerId){
		return new Trigger(TriggerType.END, layerId, -1);
	}
	public static Trigger CreateLoop(int layerId, String videoName){
		Trigger t = new Trigger(TriggerType.END, layerId, -1);
		t.loop = true;
		t.addCommand(new LoopCommand(layerId));
		return t;
	}

	private Trigger(TriggerType type, int layerId, long targetFrame) {
		this.type = type;
		this.targetFrame = targetFrame;
		this.layerId = layerId;
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