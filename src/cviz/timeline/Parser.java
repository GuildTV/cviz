package cviz.timeline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	private LinkedList<Trigger> commands = new LinkedList<>();

    Trigger currentTrigger = null;

    private Pattern pattern = Pattern.compile("([A-Z0-9]+)+");
	private BufferedReader reader;

	private Parser(BufferedReader reader){
		this.reader = reader;
	}

    private void Parse(){
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                ParseLine(line);
            }
        } catch (IOException e){
            commands = null;
        }
    }

    private void ParseLine(String line){
        if(line.startsWith("@")) {
            // trigger line
            ParseTrigger(line);
        }
        else if(line.startsWith("}")) {
            commands.add(currentTrigger);
            currentTrigger = null;
        }
        else if(line.startsWith("#")) {
            // comment
        }
        else {
            String[] parts = line.trim().split(" ");
            short layer = Short.parseShort(parts[0]);
            CommandType action = parseCommandType(parts[1]);
            if(parts.length >= 4) {
                currentTrigger.addCommand(new Command(layer, action, parts[2], parts[3]));
            }
            else if(parts.length == 3) {
                currentTrigger.addCommand(new Command(layer, action, parts[2]));
            }
            else if(parts.length == 2) {
                currentTrigger.addCommand(new Command(layer, action));
            }
        }
    }


    private void ParseTrigger(String line){
        Matcher matcher;
        String qualifier;

        matcher = pattern.matcher(line);
        if(matcher.find()) {
            qualifier = matcher.group();

            if(qualifier.equals("END")) {
                matcher.find();
                currentTrigger = Trigger.CreateEnd(Short.parseShort(matcher.group()));
            }
            else if(qualifier.equals("Q")) {
                currentTrigger = Trigger.CreateCue();
            }
            else {
                matcher.find();
                currentTrigger = Trigger.CreateFrame(Short.parseShort(matcher.group()), Long.parseLong(qualifier));
            }
        }
        else {
            currentTrigger = Trigger.CreateImmediate();
        }
    }


	public static LinkedList<Trigger> Parse(String path) {
        LinkedList<Trigger> commands = null;

		FileReader fr = null;
        BufferedReader br = null;

		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);

            commands = Parse(br);

        } catch (IOException e) {
			e.printStackTrace();
		} finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            }catch (IOException e){
            }
        }
		return commands;
	}

    public static LinkedList<Trigger> Parse(BufferedReader reader) {
        Parser parser = new Parser(reader);
        parser.Parse();
        return parser.commands;
    }


    private static CommandType parseCommandType(String s) {
        switch(s) {
            case "PLAY":
                return CommandType.PLAY;
            case "LOAD":
                return CommandType.LOAD;
            case "STOP":
                return CommandType.STOP;
            case "LOOP":
                return CommandType.LOOP;
            case "PAUSE":
                return CommandType.PAUSE;
            case "RESUME":
                return CommandType.RESUME;
            case "CLEAR":
                return CommandType.CLEAR;
            case "CGADD":
                return CommandType.CGADD;
            case "CGNEXT":
                return CommandType.CGNEXT;
            case "CGPLAY":
                return CommandType.CGPLAY;
            case "CGREMOVE":
                return CommandType.CGREMOVE;
            case "CGSTOP":
                return CommandType.CGSTOP;
        }
        System.err.println("Bad command type " + s);
        return CommandType.UNKNOWN;
    }

}
