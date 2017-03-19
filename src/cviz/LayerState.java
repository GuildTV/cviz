package cviz;

/**
 * Track the layer state to deal with stopping on not the last frame
 */
public class LayerState {
	private final String videoName;
	private long lastFrame;
	
	public LayerState(String videoName) {
		this.videoName = videoName;
		lastFrame = 0;
	}

	public String getVideoName() {
		return videoName;
	}
	
	public long getLastFrame() {
		return lastFrame;
	}
	
	void setLastFrame(long lastFrame) {
		this.lastFrame = lastFrame;
	}

	public String toString() {
		return "LayerState: " + videoName + " " + lastFrame;
	}
}