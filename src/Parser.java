import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	public static LinkedList<Trigger> parseTimeline(String path) {
		LinkedList<Trigger> commands = new LinkedList<Trigger>();
		
		FileReader fr;
		try {
			String line;
			
			fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			
			Pattern pattern = Pattern.compile("([A-Z0-9]+)+");
			Matcher matcher;
			String qualifier;
			
			Trigger currentTrigger = null;
			
			while((line = br.readLine()) != null) {
				if(line.startsWith("@")) {
					// trigger line
					matcher = pattern.matcher(line);
					if(matcher.find()) {
						qualifier = matcher.group();
						
						if(qualifier.equals("END")) {
							matcher.find();
							currentTrigger = new Trigger(Trigger.END, Short.parseShort(matcher.group()));
						}
						else if(qualifier.equals("Q")) {
							currentTrigger = new Trigger(Trigger.QUEUED);
						}
						else {
							matcher.find();
							currentTrigger = new Trigger(Trigger.FRAME, Long.parseLong(qualifier), Short.parseShort(matcher.group()));
						}
					}
					else {
						currentTrigger = new Trigger(Trigger.IMMEDIATE);
					}
				}
				else if(line.startsWith("}")) {
					commands.add(currentTrigger);
					continue;
				}
				else if(line.startsWith("#")) {
					// comment
					continue;
				}
				else {
					String[] parts = line.trim().split(" ");
					short layer = Short.parseShort(parts[0]);
					byte action = Command.lookupAction(parts[1]);
					if(parts.length == 3) {
						currentTrigger.addCommand(new Command(layer, action, parts[2]));
					}
					else if(parts.length == 2) {
						currentTrigger.addCommand(new Command(layer, action));
					}
				}
			}
			br.close();
		} catch(FileNotFoundException e) {
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return commands;
	}
	
	public static void main(String[] args) {
		LinkedList<Trigger> commands = Parser.parseTimeline("c:\\caspar\\new.tl");
		Iterator<Trigger> i = commands.iterator();
		while(i.hasNext()) {
			Trigger t = i.next();
			System.out.println("TRIGGER: " + t.getType() + " " + t.getTime() + " " + t.getLayer());
			Command c;
			while((c = t.getNextCommand()) != null) {
				System.out.println("\tCOMMAND: " + c.getLayer() + " " + c.getAction() + " " + c.getName());
			}
		}
	}
	
}
