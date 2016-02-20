package cviz;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import se.svt.caspar.amcp.AmcpChannel;
import se.svt.caspar.amcp.AmcpLayer;
import se.svt.caspar.producer.Video;

/**
 * Wrapper for parsed lines in the timeline
 * Performs the action when called by OSC
 */
public class Command {
	private int layer;
	private CommandType action;
	private String name;
	
	public Command(int layer, CommandType action, String name) {
		this.layer = layer;
		this.action = action;
		this.name = name;
	}
	
	public Command(int layer, CommandType action) {
		this.layer = layer;
		this.action = action;
	}
	
	public String getName() {
		return name;
	}
	
	public int getLayer() {
		return layer;
	}
	
	public CommandType getAction() {
		return action;
	}
	
	public static CommandType parseCommandType(String s) {
		switch(s) {
		case "PLAY":
			return CommandType.PLAY;
		case "LOAD":
			return CommandType.LOAD;
		case "STOP":
			return CommandType.STOP;
		case "LOOP":
			return CommandType.LOOP;
		case "PAUSE":
			return CommandType.PAUSE;
		case "RESUME":
			return CommandType.RESUME;
		}
		return CommandType.UNKNOWN;
	}
	
	public static void execute(Command c, ConcurrentHashMap<Integer, LayerState> currentLayer, AmcpChannel channel,
							   CopyOnWriteArrayList<Trigger> activeTriggers) {
		AmcpLayer layer = new AmcpLayer(channel, c.getLayer());
		if(c.getAction() == CommandType.PLAY) {
			layer.play();
		}
		else if(c.getAction() == CommandType.LOAD) {
			layer.loadBg(new Video(c.getName()));
			currentLayer.put(layer.layerId(), new LayerState(c.getName()));
		}
		else if(c.getAction() == CommandType.STOP) {
			layer.stop();
		}
		else if(c.getAction() == CommandType.PAUSE) {
			layer.pause();
		}
		else if(c.getAction() == CommandType.RESUME) {
			layer.sendCommand("RESUME");
		}
		else if(c.getAction() == CommandType.LOOP) {
			layer.play();
			layer.loadBg(new Video(currentLayer.get(layer.layerId()).getName()));
			currentLayer.put(layer.layerId(), currentLayer.get(layer.layerId()));
			Trigger t = new Trigger(TriggerType.END, c.getLayer());
			t.setLoop();
			t.addCommand(new Command(c.getLayer(), CommandType.LOOP, currentLayer.get(layer.layerId()).getName()));
			activeTriggers.add(t);
		}
		else {
			System.err.println("Invalid command");
		}
	}
	
	public String toString() {
		return "COMMAND: " + getLayer() + " " + getAction() + " " + getName();
	}
	
}
