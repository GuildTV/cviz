public class LayerInfo {

	private String name;
	private long lastFrame;
	
	public LayerInfo(String name) {
		this.name = name;
		lastFrame = 0;
	}
	
	public LayerInfo(String name, long lastFrame) {
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
		return "LayerInfo: " + name + " " + lastFrame;
	}
}