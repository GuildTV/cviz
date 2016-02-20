package cviz;

import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import cviz.timeline.Command;
import cviz.timeline.Trigger;
import cviz.timeline.TriggerType;
import lib.ResettableCountDownLatch;
import se.svt.caspar.amcp.AmcpChannel;

public class OSC implements Runnable {
	
	private ConcurrentHashMap<Integer, LayerState> currentLayer;
	private CopyOnWriteArrayList<Trigger> triggers;
	private ResettableCountDownLatch latch;
	private AmcpChannel channel;

    private int oscPort;
	
	private boolean cued = false;
	private boolean checkNonLoop = false;
	
	public OSC(ConcurrentHashMap<Integer, LayerState> currentLayer, CopyOnWriteArrayList<Trigger> triggers,
			   ResettableCountDownLatch latch, AmcpChannel channel, int oscPort) {
		this.currentLayer = currentLayer;
		this.triggers = triggers;
		this.latch = latch;
		this.channel = channel;
        this.oscPort = oscPort;
	}
	
	public void cue() {
		cued = true;
	}
	
	public void checkNonLoop() {
		checkNonLoop = true;
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
		long currentFrame;
		long targetFrame;

		public void acceptMessage(java.util.Date time, OSCMessage message) {
            if(message.getAddress()
                    .matches("/channel/1/stage/layer/[0-9]*/file/frame")) {
                int layer = Integer.parseInt(message.getAddress().split("/")[5]);
                boolean anyNonLoop = false;
                for(Trigger t : triggers) {
                    if(!t.isLoop()) {
                        anyNonLoop = true;
                    }
                    if(t.getType() == TriggerType.QUEUED) {
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
                    else if(t.getType() == TriggerType.FRAME || t.getType() == TriggerType.END) {
                        if(t.getLayer() == layer) {
                            currentFrame = (long) message.getArguments().get(0);
                            if(t.getType() == TriggerType.END) {
                                targetFrame = (long) message.getArguments().get(1);
                            }
                            else {
                                targetFrame = t.getTime();
                            }
                            if(currentLayer.get(layer).getLastFrame() == currentFrame &&
                                    targetFrame > currentFrame) {
                                // the video didn't play to the end for some reason, move on
                                System.err.println("Loop didn't reach the end, check your video!");
                                Command c;
                                while((c = t.getNextCommand()) != null) {
                                    Command.execute(c, currentLayer, channel, triggers);
                                }
                                triggers.remove(t);
                            }
                            else if(t.hasWaited()) {
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

                if(currentLayer.containsKey(layer)) {
                    currentLayer.get(layer).setLastFrame(currentFrame);
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
            else if(triggers.size() == 0) {
                if(checkNonLoop) {
                    if(latch.getCount() == 1) {
                        latch.countDown();
                        checkNonLoop = false;
                    }
                }
            }
            else { // queued triggers will get stuck if only one layer active (and is paused!)
                for(Trigger t : triggers) {
                    if(t.getType() == TriggerType.QUEUED) {
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
                }
            }
        }
	}
}
