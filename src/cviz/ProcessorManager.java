package cviz;

import cviz.control.IControlInterface;
import cviz.timeline.Parser;
import cviz.timeline.Trigger;
import se.svt.caspar.amcp.AmcpCasparDevice;
import se.svt.caspar.amcp.AmcpChannel;

import java.io.File;
import java.util.LinkedList;

public class ProcessorManager {
    private static final int oscPort = 5253; // TODO - make this dynamic
    private static final int amcpChannel = 1; // TODO - make dynamic

    private static final String timelinePath = "./";
    private static final String timelineExt = ".tl";

    private IProcessor processor;
    private OSC oscWrapper;

    private IControlInterface controlInterface;
    private AmcpCasparDevice host;

    public ProcessorManager(){
        oscWrapper = new OSC(this, oscPort);
        new Thread(oscWrapper).start();

        host = new AmcpCasparDevice("127.0.0.1", 5250); // TODO - make dynamic
    }

    public void bindInterface(IControlInterface newInterface){
        controlInterface = newInterface;

        controlInterface.setWaitingForTimeline();
    }

    public boolean loadTimeline(String name){
        if(processor != null && processor.isRunning()) {
            System.err.println("Cannot load timeline when one is already running");
            return false;
        }
        processor = null;

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
        processor = new Processor(channel, controlInterface, timeline);

        System.out.println("Timeline ready");
        controlInterface.setTimelineLoaded();

        return true;
    }

    public boolean startTimeline(){
        if(processor == null)
            return false;

        if(processor.isRunning())
            return false;

        new Thread(processor).start();
        return true;
    }

    public void killTimeline(){
        if(processor != null)
            processor.kill();
    }

    public void receivedCue(){
        if(processor == null)
            return;

        processor.receivedCue();
    }

    public void receiveVideoFrame(int channel, int layer, long frame, long totalFrames){
        if(channel != amcpChannel)
            return;

        if(processor == null)
            return;

        processor.receiveVideoFrame(layer, frame, totalFrames);
    }
}
