package cviz;

import cviz.timeline.Command;
import cviz.timeline.CommandType;
import cviz.timeline.Trigger;
import cviz.timeline.TriggerType;
import se.svt.caspar.amcp.AmcpChannel;
import se.svt.caspar.amcp.AmcpLayer;
import se.svt.caspar.producer.Video;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Processor implements IProcessor, Runnable {
    private AmcpChannel channel;
    private LinkedList<Trigger> triggers;
    private CopyOnWriteArrayList<Trigger> activeTriggers = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<Integer, LayerState> currentLayerState = new ConcurrentHashMap<>();

    public Processor(AmcpChannel channel, LinkedList<Trigger> triggers){
        this.channel = channel;
        this.triggers = triggers;
    }

    @Override
    public void run() {
        System.out.println("Starting timeline");

        // set some triggers as active
        promoteTriggersToActive();

        // run any immediate triggers
        receiveVideoFrame(-1, 0, 100);

        while(!triggers.isEmpty() && !activeTriggers.isEmpty()){
            // TODO - hmm... probably need to be doing something...
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Finished running timeline");
    }

    private boolean promoteTriggersToActive(){
        int moved = 0;

        do{
            Trigger t = triggers.pop();
            moved++;

            activeTriggers.add(t);

            // we only want to add up until a manual trigger
            if(t.getType() == TriggerType.QUEUED)
                break;

        } while(!triggers.isEmpty());

        return moved > 0;
    }

    @Override
    public int getChannelNumber(){
        return channel.channelId();
    }

    @Override
    public synchronized void receivedCue(){
        // TODO - maybe this should be buffered, otherwise there could be some timing issues

        // find trigger to cue
        Optional<Trigger> queued = activeTriggers.stream().filter(t -> t.getType() == TriggerType.QUEUED).findFirst();
        if(!queued.isPresent()){
            System.out.println("Received a cue without a trigger to fire");
            return;
        }

        // run the trigger
        executeTrigger(queued.get());
        activeTriggers.remove(queued.get());

        if(!promoteTriggersToActive()){
            System.out.println("Reached end of timeline");
        }
    }

    @Override
    public synchronized void receiveVideoFrame(int layer, long frame, long totalFrames){
        for(Trigger t: activeTriggers){
            if(t.getType() == TriggerType.IMMEDIATE){
                executeTrigger(t);
                continue;
            }

            if(t.getLayer() != layer)
                continue;

            // determine the frame we are aiming for
            long targetFrame = totalFrames;
            if(t.getType() != TriggerType.END) {
                targetFrame = t.getTime();
            }

            LayerState state = currentLayerState.get(layer);
            if(state == null){
                System.out.println("Tried to get state and failed");
                state = new LayerState("Something");
                currentLayerState.put(layer, state);
            }

            // TODO - this check needs to ensure that an appropriate amount of time has passed
            if(state.getLastFrame() == frame && targetFrame > frame) {
                // the video didn't play to the end for some reason, move on
                System.err.println("Loop didn't reach the end, check your video!");

                executeTrigger(t);
            }
            else if(t.hasWaited()) {
                // do it
                executeTrigger(t);
            }
            else if(frame == targetFrame) {
                t.setWaited();
            }
        }

        if(currentLayerState.containsKey(layer)) {
            currentLayerState.get(layer).setLastFrame(frame);
        }
    }

    private void executeTrigger(Trigger trigger){
        trigger.getCommands().forEach(this::executeCommand);
        activeTriggers.remove(trigger);
    }

    public void executeCommand(Command c) {
        AmcpLayer layer = new AmcpLayer(channel, c.getLayer());

        switch(c.getAction()){
            case PLAY:
                layer.play();
                break;

            case LOAD:
                layer.loadBg(new Video(c.getName()));
                currentLayerState.put(layer.layerId(), new LayerState(c.getName()));
                break;

            case STOP:
                layer.stop();
                break;

            case PAUSE:
                layer.pause();
                break;

            case RESUME:
                layer.sendCommand("RESUME");
                break;

            case LOOP:
                layer.play();
                layer.loadBg(new Video(currentLayerState.get(layer.layerId()).getName()));
                currentLayerState.put(layer.layerId(), currentLayerState.get(layer.layerId()));
                Trigger t = new Trigger(TriggerType.END, c.getLayer());
                t.setLoop();
                t.addCommand(new Command(c.getLayer(), CommandType.LOOP, currentLayerState.get(layer.layerId()).getName()));
                activeTriggers.add(t);
                break;

            default:
                System.err.println("Invalid command");
        }
    }

}
