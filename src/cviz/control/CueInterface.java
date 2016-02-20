package cviz.control;

import cviz.OSC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CueInterface implements Runnable {

	private OSC osc;
	
	public CueInterface(OSC osc) {
		this.osc = osc;
	}
	
	public void run() {
		while(true) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String s = br.readLine();
				if(s.equalsIgnoreCase("GO")) {
					osc.cue();
				}			
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

}
