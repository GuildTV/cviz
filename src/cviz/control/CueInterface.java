package cviz.control;

import cviz.IProcessor;
import cviz.OSC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CueInterface implements Runnable {

	private IProcessor processor;
	
	public CueInterface(IProcessor processor) {
		this.processor = processor;
	}
	
	public void run() {
		while(true) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String s = br.readLine();
				if(s.equalsIgnoreCase("GO")) {
					processor.receivedCue();
				}			
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

}
