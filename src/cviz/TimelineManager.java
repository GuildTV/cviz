package cviz;

import cviz.control.IControlInterface;
import cviz.timeline.Parser;
import cviz.timeline.Trigger;
import se.svt.caspar.amcp.AmcpCasparDevice;
import se.svt.caspar.amcp.AmcpChannel;

import java.io.File;
import java.util.LinkedList;

public class TimelineManager {
    private static final int oscPort = 5253; // TODO - make this dynamic
    private static final int amcpChannel = 1; // TODO - make dynamic

    private static final String timelinePath = "./";
    private static final String timelineExt = ".tl";

    private final AmcpCasparDevice host;

    private IControlInterface controlInterface;

    private ITimeline timeline;

    public TimelineManager(){
        OSC oscWrapper = new OSC(this, oscPort);
        new Thread(oscWrapper).start();

        host = new AmcpCasparDevice("127.0.0.1", 5250); // TODO - make dynamic
    }

    public void bindInterface(IControlInterface newInterface){
        controlInterface = newInterface;

        controlInterface.notifyState(TimelineState.CLEAR);
    }

    public boolean loadTimeline(String name){
        if(timeline != null && timeline.isRunning()) {
            System.err.println("Cannot load timeline when one is already running");
            return false;
        }
        timeline = null;

        File file = new File(timelinePath + name + timelineExt);
        if(!file.exists() || !file.isFile()){
            System.err.println("Cannot find new timeline file");
            return false;
        }

        LinkedList<Trigger> timeline = Parser.Parse(file.getAbsolutePath());
        if(timeline == null){
            System.err.println("Failed to parse timeline file");
            return false;
        }

        AmcpChannel channel = new AmcpChannel(host, amcpChannel);
        this.timeline = new Timeline(channel, controlInterface, timeline);

        System.out.println("Timeline ready");

        return true;
    }

    public boolean startTimeline(){
        if(timeline == null)
            return false;

        if(timeline.isRunning())
            return false;

        new Thread(timeline).start();
        return true;
    }

    public void killTimeline(){
        if(timeline != null)
            timeline.kill();
    }

    public void triggerCue(){
        if(timeline == null)
            return;

        timeline.triggerCue();
    }

    public void triggerOnVideoFrame(int channel, int layer, long frame, long totalFrames){
        if(channel != amcpChannel)
            return;

        if(timeline == null)
            return;

        timeline.triggerOnVideoFrame(layer, frame, totalFrames);
    }
}
