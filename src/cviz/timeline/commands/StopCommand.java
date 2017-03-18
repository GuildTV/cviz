package cviz.timeline.commands;

import cviz.ITimeline;
import cviz.timeline.Trigger;

import java.util.List;
import java.util.stream.Collectors;

public class StopCommand extends AmcpCommand {
    public StopCommand(int layerId, String command) {
        super(layerId, command);
    }

    @Override
    public boolean execute(ITimeline timeline) {
        List<Trigger> oldTriggers = timeline.getActiveTriggers().stream()
                .filter(t -> t.isLoop() && t.getLayerId() == getLayerId())
                .collect(Collectors.toList());
        timeline.getActiveTriggers().removeAll(oldTriggers);

        return super.execute(timeline);
    }

    @Override
    public String getCommandName() {
        return "StopCommand";
    }
}
