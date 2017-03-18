package cviz.timeline;

import cviz.timeline.commands.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
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


    private void ParseTrigger(String line) throws Exception {
        Matcher matcher;
        String qualifier;

        matcher = pattern.matcher(line);
        if(matcher.find()) {
            qualifier = matcher.group();

            switch (qualifier) {
                case "END":
                    if (!matcher.find())
                        throw new Exception("Failed to match after end trigger");
                    currentTrigger = Trigger.CreateEnd(Short.parseShort(matcher.group()));
                    break;
                case "Q":
                    currentTrigger = Trigger.CreateCue();
                    break;
                default:
                    if (!matcher.find())
                        throw new Exception("Failed to match after frame trigger");
                    currentTrigger = Trigger.CreateFrame(Short.parseShort(matcher.group()), Long.parseLong(qualifier));
                    break;
            }
        }
        else {
            currentTrigger = Trigger.CreateSetup();
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
        String command = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

        switch(parts[1]) {
            case "LOADBG":
                return new LoadCommand(layerId, command, parts[2]);
            case "STOP":
                return new StopCommand(layerId, command);
            case "LOOP":
                return new LoopCommand(layerId);
            case "CLEAR":
                return new ClearCommand(layerId);
            case "CGADD":
                return new CgAddCommand(layerId, parts[2], parts[3]);
            default:
                return new AmcpCommand(layerId, command);
        }
    }
}
