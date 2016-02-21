package cviz.control;

import cviz.TimelineManager;
import cviz.TimelineState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ConsoleControlInterface implements IControlInterface {

	private final TimelineManager manager;

	private TimelineState state = TimelineState.CLEAR;

	public ConsoleControlInterface(TimelineManager manager) {
		this.manager = manager;
	}

	public void run() {
		while(true) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String s = br.readLine();

				if(s.equalsIgnoreCase("KILL")) {
					manager.killTimeline();
				} else {
					switch(state){
						case ERROR:
						case CLEAR:
							manager.loadTimeline(s);
							break;
						case READY:
							manager.startTimeline(new HashMap<>());
							break;
						case CUE:
							manager.triggerCue();
							break;
						case RUN:
							break;
						default:
							System.err.println("UNKNOWN STATE. Ignoring input");
							break;
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyState(TimelineState state) {
		if(this.state == state) return;

		this.state = state;

		switch(state){
			case ERROR:
				System.err.println("Timeline had an error");
				break;

			case READY:
				System.out.println("Loaded timeline: ");
				break;

			case CUE:
				System.out.println("\nWaiting for cue: \n");
				break;

			case RUN:
				System.out.println("Running: ");
				break;

			case CLEAR:
				System.out.println("\n Timeline unloaded. Waiting for new timeline: \n");
				break;

			default:
				System.err.println("Unknown timeline state: " + state);
				break;
		}
	}

}
