package cviz.timeline;

import cviz.timeline.commands.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern setupTriggerPattern = Pattern.compile("^@ \\{$");
    private static final Pattern cueTriggerPattern = Pattern.compile("^@Q (.*) \\{$");
    private static final Pattern endTriggerPattern = Pattern.compile("^@END ([0-9]+) \\{$");
    private static final Pattern frameTriggerPattern = Pattern.compile("^@([0-9]+) ([0-9]+) \\{$");

    private static final Pattern loadCommandPattern = Pattern.compile("^(LOAD|LOADBG) ([\"].+?[\"]|[^ ]+)");
    private static final Pattern stopCommandPattern = Pattern.compile("^STOP");
    private static final Pattern loopCommandPattern = Pattern.compile("^LOOP");
    private static final Pattern clearCommandPattern = Pattern.compile("^CLEAR");

	private LinkedList<Trigger> triggers = new LinkedList<>();

    private Trigger currentTrigger = null;

	private final BufferedReader reader;

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
            triggers = null;
        } catch (Exception e) {
            e.printStackTrace();
            triggers = null;
        }
    }

    private void ParseLine(String line) throws Exception {
        line = line.trim();

        if(line.startsWith("@")) {
            // trigger line
            currentTrigger = ParseTrigger(line);
            if (currentTrigger == null)
                throw new Exception("Failed to parse trigger: " + line);
        }
        else if(line.startsWith("}")) {
            triggers.add(currentTrigger);
            currentTrigger = null;
        }
        else if(line.startsWith("#") || line.length() == 0) {
            // comment or blank line
        }
        else {
            if (currentTrigger == null)
                throw new Exception("Failed to add command outside of trigger: " + line);

            currentTrigger.addCommand(ParseCommand(line));
        }
    }

    private static Trigger ParseTrigger(String line) {
        Matcher matcher = setupTriggerPattern.matcher(line);
        if (matcher.find())
            return Trigger.CreateSetup();

        matcher = cueTriggerPattern.matcher(line);
        if (matcher.find())
            return Trigger.CreateCue(matcher.group(1));

        matcher = endTriggerPattern.matcher(line);
        if (matcher.find())
            return  Trigger.CreateEnd(Short.parseShort(matcher.group(1)));

        matcher = frameTriggerPattern.matcher(line);
        if (matcher.find())
            return  Trigger.CreateFrame(Short.parseShort(matcher.group(2)), Long.parseLong(matcher.group(1)));

        return null;
    }

    private static ICommand ParseCommand(String line) throws Exception {
        String[] parts = line.split(" ", 2);
        short layerId = Short.parseShort(parts[0]);
        String command = parts[1];

        Matcher matcher = loadCommandPattern.matcher(command);
        if (matcher.find())
            return new LoadCommand(layerId, command, matcher.group(2));

        matcher = stopCommandPattern.matcher(command);
        if (matcher.find())
            return new StopCommand(layerId, command);

        matcher = loopCommandPattern.matcher(command);
        if (matcher.find())
            return new LoopCommand(layerId);

        matcher = clearCommandPattern.matcher(command);
        if (matcher.find())
            return new ClearCommand(layerId);

        return new AmcpCommand(layerId, command);
    }

    public static LinkedList<Trigger> ParseFile(String path) {
        LinkedList<Trigger> commands = null;

        FileReader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(path);
            br = new BufferedReader(fr);

            commands = ParseBuffer(br);

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

    public static LinkedList<Trigger> ParseBuffer(BufferedReader reader) {
        Parser parser = new Parser(reader);
        parser.Parse();
        return parser.triggers;
    }

}
