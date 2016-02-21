package cviz.timeline;

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

    public String toString() {
        return "COMMAND: " + getLayer() + " " + getAction() + " " + getName();
    }
}
