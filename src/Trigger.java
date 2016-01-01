import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Trigger {
	static byte IMMEDIATE = 0;
	static byte FRAME = 1;
	static byte END = 2;
	static byte QUEUED = 3;
	
	private byte type;
	private long time;
	private int layer;
	private boolean loop = false;
	private boolean waited = false;
	private boolean cued = false;
	
	private LinkedList<Command> commands;
	
	public Trigger(byte type) {
		commands = new LinkedList<Command>();
		this.type = type;
		if(type == Trigger.QUEUED) {
			layer = -1;
			time = -1;
		}
	}
	
	public Trigger(byte type, int layer) {
		commands = new LinkedList<Command>();
		this.type = type;
		this.layer = layer;
		if(type == Trigger.END) {
			time = -1;
		}
	}
	
	public Trigger(byte type, long time, int layer) {
		commands = new LinkedList<Command>();
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
	
	public Command getNextCommand() {
		try {
			return commands.pop();
		}
		catch(NoSuchElementException e) {
			return null;
		}
	}
	
	public Command getNextCommand(boolean peek) {
		if(peek) {
			return commands.peek();
		}
		else {
			return getNextCommand();
		}
	}

	public byte getType() {
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
	
	public boolean beenCued() {
		return cued;
	}
	
	public void cue() {
		cued = true;
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
