package cviz;

import cviz.config.Config;
import cviz.config.ChannelConfig;
import cviz.control.IControlInterface;
import cviz.state.State;
import cviz.timeline.Parser;
import cviz.timeline.Trigger;
import se.svt.caspar.amcp.AmcpCasparDevice;
import se.svt.caspar.amcp.AmcpChannel;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TimelineManager {
    public static final String timelinePath = "./";
    public static final String timelineExt = ".tl";

    private final AmcpCasparDevice host;
    private final Config config;
    private final Map<String, Timeline> timelines;

    private IControlInterface controlInterface;

    public TimelineManager(Config config) {
        this.config = config;
        OSC oscWrapper = new OSC(this, config.getOscPort());
        new Thread(oscWrapper).start();

        host = new AmcpCasparDevice(config.getCasparHost(), config.getCasparPort());
        timelines = new HashMap<String, Timeline>();
    }

    public void bindInterface(IControlInterface newInterface) {
        controlInterface = newInterface;
    }

    public synchronized State[] getCompleteState(){
        return timelines.values().stream().map(t -> t.getState()).toArray(State[]::new);
    }

    public synchronized State getStateForTimelineSlot(String timelineSlot){
        return timelines.values().stream().map(t -> t.getState()).filter(s -> s.getTimelineSlot().equals(timelineSlot)).findFirst().orElse(null);
    }

    public synchronized boolean loadTimeline(String channelId, String timelineSlot, String filename, String instanceId) {
        Timeline timeline = timelines.get(timelineSlot);
        if (timeline != null && timeline.isRunning()) {
            System.err.println("Cannot load timeline to " + timelineSlot + " when one is already running");
            return false;
        }

        if (timeline != null)
            timelines.remove(channelId);

        ChannelConfig channelConfig = config.findChannelById(channelId);
        if (channelConfig == null) {
            System.err.println("Channel " + channelId + " is not defined in config");
            return false;
        }

        File file = new File(timelinePath + filename + timelineExt);
        if (!file.exists() || !file.isFile()) {
            System.err.println("Cannot find new timeline file: " + filename);
            return false;
        }

        LinkedList<Trigger> sequence = Parser.ParseFile(file.getAbsolutePath());
        if (sequence == null) {
            System.err.println("Failed to parse timeline file: " + filename);
            return false;
        }

        AmcpChannel channel = new AmcpChannel(host, channelConfig.getChannel());
        State state = new State(controlInterface, timelineSlot, filename, instanceId);
        timelines.put(timelineSlot, new Timeline(timelineSlot, channel, state, sequence));

        System.out.println("Timeline " + timelineSlot + "ready");

        return true;
    }

    public synchronized boolean startTimeline(String timelineId, HashMap<String, String> parameters) {
        Timeline timeline = timelines.get(timelineId);
        if (timeline == null)
            return false;

        if (timeline.isRunning())
            return false;

        timeline.setParameters(parameters);

        new Thread(timeline).start();
        return true;
    }

    public synchronized void killTimeline(String timelineId) {
        Timeline timeline = timelines.get(timelineId);
        if (timeline != null)
            timeline.kill();
    }

    public synchronized void triggerCue(String timelineId) {
        Timeline timeline = timelines.get(timelineId);
        if (timeline == null)
            return;

        timeline.triggerCue();
    }

    public synchronized void triggerOnVideoFrame(int channel, int layer, long frame, long totalFrames) {
        if (timelines == null)
            return;

        Timeline[] toTrigger =  timelines.values().stream().filter(t -> t.getChannelNumber() == channel).toArray(Timeline[]::new);
        for(Timeline timeline: toTrigger){
            if (timeline == null)
                return;

            timeline.triggerOnVideoFrame(layer, frame, totalFrames);
        }
    }
}
