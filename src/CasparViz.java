import java.net.SocketException;
import com.illposed.osc.*;

import se.svt.caspar.amcp.*;
import se.svt.caspar.producer.*;


public class CasparViz extends Thread {
	
	public static void main(String[] args) throws InterruptedException, SocketException {
		(new CasparViz()).start();
		/*AmcpCasparDevice host = new AmcpCasparDevice("127.0.0.1", 5250);
		
		//System.out.println(host.channels().get(0).videoMode());
		
		
		
		AmcpLayer layer = new AmcpLayer(channel, 10);
		Video v1 = new Video("TestPatterns/PAL_Test");
		v1.loop(true);
		
		System.out.println("Loading v1");
		layer.load(v1);
		
		Thread.sleep(1000);
		
		System.out.println("Playing v1");
		layer.play();
		
		
		host.close();*/
	}

	public void run() {
		AmcpCasparDevice host = new AmcpCasparDevice("127.0.0.1", 5250);
		AmcpChannel channel = new AmcpChannel(host, 1);
		AmcpLayer layer = new AmcpLayer(channel, 10);
		Video v1 = new Video("TESTPATTERNS/PAL_TEST");
		layer.load(v1);
		
		try {
			OSCPortIn receiver = new OSCPortIn(5253);
			OSCListener listener = new OSCListener() {
				boolean alreadyEnded = false;
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					/*System.out.print(message.getAddress() + "    ");
					for(Object x : message.getArguments()) {
						System.out.print(x + " : ");
					}
					System.out.println();*/
					/*if((long) message.getArguments().get(0) == (long) message.getArguments().get(1)) {
						if(alreadyEnded) {
							//System.out.println("looping");
							layer.play(v1);
							alreadyEnded = false;
						}
						else {
							alreadyEnded = true;
						}
					}*/
				}
			};
			//receiver.addListener("/channel/1/stage/layer/10/file/frame", listener);
			receiver.addListener("", listener);
			receiver.startListening();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		layer.play(v1);
		while(true);
	}

}
