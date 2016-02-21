package cviz.timeline;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Trigger {
	private TriggerType type;
	private long time;
	private int layer;
	private boolean loop = false;
	private boolean waited = false;
	
	private LinkedList<Command> commands;
	
	public Trigger(TriggerType type) {
		commands = new LinkedList<>();
		this.type = type;
		if(type == TriggerType.QUEUED) {
			layer = -1;
			time = -1;
		}
	}
	
	public Trigger(TriggerType type, int layer) {
		commands = new LinkedList<>();
		this.type = type;
		this.layer = layer;
		if(type == TriggerType.END) {
			time = -1;
		}
	}
	
	public Trigger(TriggerType type, long time, int layer) {
		commands = new LinkedList<>();
		this.type = type;
		this.time = time;
		this.layer = layer;
	}
	
	public void setLoop() {
		loop = true;
	}
	
	public void addCommand(Command c) {
		commands.add(c);
	}

    public List<Command> getCommands(){
        return commands;
    }
	
	public Command getNextCommand() {
		try {
			return commands.pop();
		}
		catch(NoSuchElementException e) {
			return null;
		}
	}

	public TriggerType getType() {
		return type;
	}
	
	public long getTime() {
		return time;
	}
	
	public int getLayer() {
		return layer;
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
		return "Trigger: " + type + " " + time + " " + layer + " loop: " + loop;
	}

	public static boolean outstandingTriggers(CopyOnWriteArrayList<Trigger> activeTriggers) {
		boolean anyNotLoop = false;
		for(Trigger t : activeTriggers) {
			if(!t.isLoop()) {
				anyNotLoop = true;
			}
		}
		return anyNotLoop;
	}
}
