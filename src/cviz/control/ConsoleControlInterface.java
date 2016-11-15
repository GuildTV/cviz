package cviz.control;

import cviz.TimelineManager;
import cviz.TimelineState;
import cviz.state.State;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ConsoleControlInterface implements IControlInterface {

	private final TimelineManager manager;

	private State state;

	public ConsoleControlInterface(TimelineManager manager) {
		this.manager = manager;
	}

	public void run() {
		while(true) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String s = br.readLine();

				if(s.equalsIgnoreCase("KILL")) {
					manager.killTimeline("default");
				} else {
					switch(state.getState()){
						case ERROR:
						case CLEAR:
							manager.loadTimeline("default", "default", s, "");
							break;
						case READY:
							manager.startTimeline("default", new HashMap<>());
							break;
						case CUE:
							manager.triggerCue("default");
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
	public void notifyState(State state) {
		this.state = state;

		switch(state.getState()){
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
