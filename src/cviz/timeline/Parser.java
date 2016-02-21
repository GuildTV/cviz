package cviz.timeline;

import cviz.timeline.commands.*;

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
        } catch (Exception e) {
            e.printStackTrace();
            commands = null;
        }
    }

    private void ParseLine(String line) throws Exception {
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
            currentTrigger.addCommand(parseCommands(parts));
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

    private static ICommand parseCommands(String[] parts) throws Exception {
        short layerId = Short.parseShort(parts[0]);

        switch(parts[1]) {
            case "PLAY":
                return new PlayCommand(layerId);
            case "LOAD":
                return new LoadCommand(layerId, parts[2]);
            case "STOP":
                return new StopCommand(layerId);
            case "LOOP":
                return new LoopCommand(layerId);
            case "PAUSE":
                return new PauseCommand(layerId);
            case "RESUME":
                return new ResumeCommand(layerId);
            case "CLEAR":
                return new ClearCommand(layerId);
            case "CGADD":
                return new CgAddCommand(layerId, parts[2], parts[3]);
            case "CGNEXT":
                return new CgNextCommand(layerId);
            case "CGPLAY":
                return new CgPlayCommand(layerId);
            case "CGREMOVE":
                return new CgRemoveCommand(layerId);
            case "CGSTOP":
                return new CgStopCommand(layerId);
        }
        System.err.println("Bad command type " + parts[1]);
        throw new Exception("Unknown command type");
    }
}
