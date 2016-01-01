import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import se.svt.caspar.amcp.AmcpChannel;
import se.svt.caspar.amcp.AmcpLayer;
import se.svt.caspar.producer.Video;

public class Command {

	static byte PLAY = 0;
	static byte LOAD = 1;
	static byte STOP = 2;
	static byte LOOP = 3;
	//static byte LOOPC = 4;
	
	private int layer;
	private byte action;
	private String name;
	
	public Command(int layer, byte action, String name) {
		this.layer = layer;
		this.action = action;
		this.name = name;
	}
	
	public Command(int layer, byte action) {
		this.layer = layer;
		this.action = action;
	}
	
	public String getName() {
		return name;
	}
	
	public int getLayer() {
		return layer;
	}
	
	public int getAction() {
		return action;
	}
	
	public static byte lookupAction(String s) {
		switch(s) {
		case "PLAY":
			return PLAY;
		case "LOAD":
			return LOAD;
		case "STOP":
			return STOP;
		case "LOOP":
			return LOOP;
		}
		return -1;
	}
	
	public static void execute(Command c, ConcurrentHashMap<Integer,String> currentLayer, AmcpChannel channel,
			CopyOnWriteArrayList<Trigger> activeTriggers) {
		execute(c, currentLayer, channel, activeTriggers, false);
	}
	
	public static void execute(Command c, ConcurrentHashMap<Integer,String> currentLayer, AmcpChannel channel,
			CopyOnWriteArrayList<Trigger> activeTriggers, boolean looping) {
		AmcpLayer layer = new AmcpLayer(channel, c.getLayer());
		if(c.getAction() == Command.PLAY) {
			layer.play();
		}
		else if(c.getAction() == Command.LOAD) {
			layer.loadBg(new Video(c.getName()));
			currentLayer.put(layer.layerId(), c.getName());
		}
		else if(c.getAction() == Command.STOP) {
			layer.stop();
		}
		else if(c.getAction() == Command.LOOP) {
			layer.play();
			layer.loadBg(new Video(currentLayer.get(layer.layerId())));
			currentLayer.put(layer.layerId(), currentLayer.get(layer.layerId()));
			Trigger t = new Trigger(Trigger.END, c.getLayer());
			t.setLoop();
			t.addCommand(new Command(c.getLayer(), Command.LOOP, currentLayer.get(layer.layerId())));
			activeTriggers.add(t);
		}
		/*else if(c.getAction() == Command.LOOPC) {
			layer.play();
			layer.loadBg(new Video(c.getName()));
			currentLayer.put(layer.layerId(), c.getName());
		}*/
		else {
			System.err.println("Invalid command");
		}
	}
	
	public String toString() {
		return "COMMAND: " + getLayer() + " " + getAction() + " " + getName();
	}
	
}
