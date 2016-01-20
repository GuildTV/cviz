import java.net.SocketException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import se.svt.caspar.amcp.*;

public class CasparViz implements Runnable {
	
	public static void main(String[] args) throws InterruptedException, SocketException {
		if (args.length < 2){
			System.out.println("Expected: 127.0.0.1 new.tl");
			return;
		}

		System.out.println("Connecting to: " + args[0]);

		ConcurrentHashMap<Integer,LayerInfo> currentLayer = new ConcurrentHashMap<Integer,LayerInfo>();
		LinkedList<Trigger> triggers = new LinkedList<Trigger>();
		CopyOnWriteArrayList<Trigger> activeTriggers = new CopyOnWriteArrayList<Trigger>();
		ResettableCountDownLatch latch = new ResettableCountDownLatch(1);
		
		AmcpCasparDevice host = new AmcpCasparDevice(args[0], 5250);
		AmcpChannel channel = new AmcpChannel(host, 1);
		
		OSC osc = new OSC(currentLayer, activeTriggers, latch, channel);
		(new Thread(osc)).start();
		(new Thread(new CasparViz(currentLayer, triggers, activeTriggers, latch, channel, osc, args[1]))).start();
		(new Thread(new CueInterface(osc))).start();
	}
	
	int currentCommand = 0;
	boolean currentCommandTrigger = false;
	int currentCommandLayer;
	private ConcurrentHashMap<Integer,LayerInfo> currentLayer;
	private LinkedList<Trigger> triggers;
	private CopyOnWriteArrayList<Trigger> activeTriggers;
	private ResettableCountDownLatch latch;
	private AmcpChannel channel;
	private OSC osc;
	private String filename;
	
	public CasparViz(ConcurrentHashMap<Integer,LayerInfo> currentLayer, LinkedList<Trigger> triggers,
			CopyOnWriteArrayList<Trigger> activeTriggers, ResettableCountDownLatch latch,
			AmcpChannel channel, OSC osc, String filename) {
		this.currentLayer = currentLayer;
		this.triggers = triggers;
		this.activeTriggers = activeTriggers;
		this.latch = latch;
		this.channel = channel;
		this.osc = osc;
		this.filename = filename;
	}

	public void run() {
		System.out.println("Caspar-timeline v0.1 running with timeline: " + filename);
		triggers = Parser.parseTimeline(filename);
		System.out.println(triggers.size() + " triggers processed");
		Trigger t;

		/*try {
			Thread.sleep(150);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}*/
		
		do {
			t = triggers.pop();
			if(t.getType() == Trigger.IMMEDIATE) {
				// do it now!
				Command c;
				while((c = t.getNextCommand()) != null) {
					Command.execute(c, currentLayer, channel, activeTriggers);
				}
			}
			else if(t.getType() == Trigger.END || t.getType() == Trigger.FRAME) {
				// make active trigger
				activeTriggers.add(t);					
			}
			else if(t.getType() == Trigger.QUEUED) {
				// construct cue trigger
				if(Trigger.outstandingTriggers(activeTriggers)) {
					osc.checkNonLoop();
					//System.out.print("Awaiting previous trigger completion before cue trigger insert...");
					try {
						latch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else {
					//System.out.println("No outstanding triggers...");
				}
				//System.out.println(" trigger inserted");
				latch.reset();
				activeTriggers.add(t);
				System.out.println("Awaiting cue on...");
				try {
					latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				latch.reset();
				System.out.println(" continuing");
			}
			if(triggers.isEmpty()) {	
				if(!activeTriggers.isEmpty()) {
					osc.checkNonLoop();
					try {
						latch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else {
					System.out.println(activeTriggers.size());
				}
				System.out.println("Done, exiting");
				System.exit(0);
			}
		} while(true);
	}

}
