package cviz.timeline.commands;

import cviz.ITimeline;
import se.svt.caspar.amcp.AmcpLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AmcpCommand extends ICommand {
    private String command;

    public AmcpCommand(int layerId, String command) {
        super(layerId);
        this.command = command;
    }

    @Override
    public boolean execute(ITimeline timeline) {
        String[] parts = command.split(" ", 2);

        return sendCommand(timeline, parts[0], parts.length > 1 ? parts[1] : "");
    }

    private boolean sendCommand(ITimeline timeline, String command, String parameters){
        AmcpLayer layer = timeline.getLayer(getLayerId());

        String[] parameterList = Arrays.stream(parameters.split(" ")).map(p -> timeline.getParameter(p)).toArray(s -> new String[s]);
        parameters = String.join(" ", parameterList);

        try {
            layer.sendCommand(command, parameters);
            return true;
        } catch (Exception e){
            System.err.println("Failed to execute command: " + e.getMessage());
            return false;
        }
    }

    public String getCommandName(){
        return "AmcpCommand";
    }

    @Override
    public String toString() {
        return getCommandName() + ": " + getLayerId() + " " + command;
    }
}
