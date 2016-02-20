package cviz.test;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import java.net.SocketException;

public class TestOSC {

	public static void main(String[] args) {
		OSCPortIn receiver = null;
		OSCListener listener = null;
		try {
			receiver = new OSCPortIn(5253);
			listener = new OSCListener() {
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					System.out.print(message.getAddress() + "    ");
					for(Object x : message.getArguments()) {
						System.out.print(x + " : ");
					}
					System.out.println();
				}
			};

			// /channel/channum/stage/layer/LAYNUM/FILE/FRAME (current/total)

			receiver.addListener("/channel/1/stage/layer/[0-9]*/file/frame", listener);
			receiver.startListening();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while(true) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
