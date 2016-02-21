package cviz.timeline;

/**
 * Wrapper for parsed lines in the timeline
 * Performs the action when called by OSC
 */
public class Command {
	private int layerId;
	private CommandType action;
	private String name;
	private String templateField;

	protected Command(int layerId, CommandType action, String name, String templateField) {
		this.layerId = layerId;
		this.action = action;
		this.name = name;
		this.templateField = templateField;
	}

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

	public String getTemplateField(){ return templateField; }
	
	public int getLayerId() {
		return layerId;
	}
	
	public CommandType getAction() {
		return action;
	}

    public String toString() {
        return "COMMAND: " + getLayerId() + " " + getAction() + " " + getName();
    }
}
