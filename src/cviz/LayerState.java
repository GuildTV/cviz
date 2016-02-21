package cviz;

/**
 * Track the layer state to deal with stopping on not the last frame
 */
public class LayerState {
	private final String videoName;
	private long previousFrame;
	
	public LayerState(String videoName) {
		this.videoName = videoName;
		previousFrame = 0;
	}

	public String getVideoName() {
		return videoName;
	}
	
	public long getPreviousFrame() {
		return previousFrame;
	}
	
	public void setPreviousFrame(long lastFrame) {
		this.previousFrame = lastFrame;
	}

	public String toString() {
		return "LayerState: " + videoName + " " + previousFrame;
	}
}