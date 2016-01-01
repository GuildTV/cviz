import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import se.svt.caspar.amcp.AmcpChannel;

public class OSC implements Runnable {
	
	boolean alreadyEnded = false;
	
	private ConcurrentHashMap<Integer,String> currentLayer;
	private CopyOnWriteArrayList<Trigger> triggers;
	private ResettableCountDownLatch latch;
	private AmcpChannel channel;
	
	private boolean cued = false;
	private boolean checkNonLoop = false;
	
	public OSC(ConcurrentHashMap<Integer,String> currentLayer, CopyOnWriteArrayList<Trigger> triggers,
			ResettableCountDownLatch latch, AmcpChannel channel) {
		this.currentLayer = currentLayer;
		this.triggers = triggers;
		this.latch = latch;
		this.channel = channel;
	}
	
	public void cue() {
		cued = true;
	}
	
	public void checkNonLoop() {
		checkNonLoop = true;
	}
	
	public void run() {
		OSCPortIn receiver = null;
		OSCListener listener = null;
		try {
			receiver = new OSCPortIn(5253);
			
			listener = new OSCListener() {
				long currentFrame;
				long targetFrame;
				
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					short layer = Short.parseShort(message.getAddress().split("/")[5]);
				
					boolean anyNonLoop = false;
					for(Trigger t : triggers) {
						if(!t.isLoop()) {
							anyNonLoop = true;
						}
						if(t.getType() == Trigger.QUEUED) {
							if(cued) {
								cued = false;
								Command c;
								while((c = t.getNextCommand()) != null) {
									Command.execute(c, currentLayer, channel, triggers);
								}
								triggers.remove(t);
								latch.countDown();
							}
						}
						else if(t.getType() == Trigger.FRAME || t.getType() == Trigger.END) {
							if(t.getLayer() == layer) {
								currentFrame = (long) message.getArguments().get(0);
								if(t.getType() == Trigger.END) {
									targetFrame = (long) message.getArguments().get(1);
								}
								else {
									targetFrame = t.getTime();
								}
								if(t.hasWaited()) {
									// do it
									Command c;
									while((c = t.getNextCommand()) != null) {
										Command.execute(c, currentLayer, channel, triggers);
									}
									triggers.remove(t);
								}
								else if(currentFrame == targetFrame) {
									t.setWaited();
								}
							}
						}
					}
					if(checkNonLoop) {
						if(!anyNonLoop) {
							if(latch.getCount() == 1) {
								latch.countDown();
								checkNonLoop = false;
							}
						}						
					}
				}
			};

			// /channel/channum/stage/layer/LAYNUM/FILE/FRAME (current/total)
			
			receiver.addListener("/channel/1/stage/layer/[0-9]*/file/frame", listener);
			receiver.startListening();
			System.out.println("OSC started");
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
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
