package cviz;

import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

public class OSC implements Runnable {
    private TimelineManager manager;
    private int oscPort;

	private Pattern pattern = Pattern.compile("/channel/([0-9]+)/stage/layer/([0-9]+)/file/frame");

	public OSC(TimelineManager manager, int oscPort) {
        this.oscPort = oscPort;
        this.manager = manager;
	}
	
	public void run() {
		OSCPortIn receiver;
		OSCListener listener;
		try {
			receiver = new OSCPortIn(oscPort);
			listener = new OSCListener();

			receiver.addListener("", listener);
			receiver.startListening();
			System.out.println("OSC listener started");
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private class OSCListener implements com.illposed.osc.OSCListener {
		public void acceptMessage(java.util.Date time, OSCMessage message) {
			Matcher matcher = pattern.matcher(message.getAddress());

			if(matcher.find()) {
				int channelNumber = Integer.parseInt(matcher.group(1));
				int layer = Integer.parseInt(matcher.group(2));

				long frame = (long) message.getArguments().get(0);
                long totalFrames = (long) message.getArguments().get(1);

                manager.triggerOnVideoFrame(channelNumber, layer, frame, totalFrames);
            }
        }
	}
}
