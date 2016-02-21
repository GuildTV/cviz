package cviz.timeline;

/**
 * Wrapper for parsed lines in the timeline
 * Performs the action when called by OSC
 */
public class Command {
	private int layerId;
	private CommandType action;
	private String name;
	
	protected Command(int layerId, CommandType action, String name) {
		this.layerId = layerId;
		this.action = action;
		this.name = name;
	}

	protected Command(int layerId, CommandType action) {
		this.layerId = layerId;
		this.action = action;
	}
	
	public String getName() {
		return name;
	}
	
	public int getLayerId() {
		return layerId;
	}
	
	public CommandType getAction() {
		return action;
	}

	protected static CommandType parseCommandType(String s) {
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
        return "COMMAND: " + getLayerId() + " " + getAction() + " " + getName();
    }
}
