package cviz.control;

import cviz.TimelineManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleControlInterface implements IControlInterface {

	private TimelineManager manager;
	
	public ConsoleControlInterface(TimelineManager manager) {
		this.manager = manager;
	}

	private boolean waitingForTimeline = false;
	private boolean timelineLoaded = false;

	private boolean waitingForCue = false;
	
	public void run() {
		while(true) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String s = br.readLine();
				if(s.equalsIgnoreCase("KILL")) {
					manager.killTimeline();
				} else if(waitingForTimeline) {
					manager.loadTimeline(s);
					waitingForTimeline = false;
				} else if(timelineLoaded){
					manager.startTimeline();
					timelineLoaded = false;
				} else if(waitingForCue) {
					manager.triggerCue();
				} else {
					System.out.println("Unknown command");
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setWaitingForTimeline(){
		waitingForTimeline = true;

		System.out.println("\nWaiting for a new timeline: \n");
	}

	@Override
	public void setTimelineLoaded() {
		timelineLoaded = true;
		System.out.println("Loaded timeline: ");
	}

	@Override
	public void setWaitingForCue() {
		if(waitingForCue) return;
		waitingForCue = true;

		System.out.println("\nWaiting for cue: \n");
	}
}
