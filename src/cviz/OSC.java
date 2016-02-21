package cviz;

import java.net.SocketException;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

public class OSC implements Runnable {

    private IProcessor processor; //TODO - list of processors for different channels

    private int oscPort;

	public OSC(IProcessor processor, int oscPort) {
        this.oscPort = oscPort;
        this.processor = processor;
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
            if(message.getAddress()
                    .matches("/channel/[0-9]+/stage/layer/[0-9]+/file/frame")) {
                int channelNumber = Integer.parseInt(message.getAddress().split("/")[2]);
                int layer = Integer.parseInt(message.getAddress().split("/")[5]);

                if(processor.getChannelNumber() != channelNumber)
                    return;

                long frame = (long) message.getArguments().get(0);
                long totalFrames = (long) message.getArguments().get(1);

                processor.receiveVideoFrame(layer, frame, totalFrames);
            }
        }
	}
}
