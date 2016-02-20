package cviz;

/**
 * Track the layer state to deal with stopping on not the last frame
 */
public class LayerState {

	private String name;
	private long lastFrame;
	
	public LayerState(String name) {
		this.name = name;
		lastFrame = 0;
	}
	
	public LayerState(String name, long lastFrame) {
		this.name = name;
		this.lastFrame = lastFrame;
	}
	
	public String getName() {
		return name;
	}
	
	public long getLastFrame() {
		return lastFrame;
	}
	
	public void setLastFrame(long lastFrame) {
		this.lastFrame = lastFrame; 
	}

	public String toString() {
		return "LayerState: " + name + " " + lastFrame;
	}
}