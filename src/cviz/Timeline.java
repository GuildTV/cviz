package cviz;

import cviz.config.TimelineConfig;
import cviz.control.IControlInterface;
import cviz.timeline.Trigger;
import cviz.timeline.TriggerType;
import cviz.timeline.commands.ClearCommand;
import cviz.timeline.commands.ICommand;
import se.svt.caspar.amcp.AmcpChannel;
import se.svt.caspar.amcp.AmcpLayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Timeline implements ITimeline, Runnable {
    private final TimelineConfig config;
    private final AmcpChannel channel;
    private final LinkedList<Trigger> triggers;
    private final CopyOnWriteArrayList<Trigger> activeTriggers = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Integer, LayerState> currentLayerState = new ConcurrentHashMap<>();
    private final Set<Integer> usedLayers = new HashSet<>();
    private final HashMap<Integer, AmcpLayer> layerCache = new HashMap<>();

    private final IControlInterface controlInterface;

    private HashMap<String, String> templateData;
    private boolean running = false;
    private boolean killNow = false;

    public Timeline(TimelineConfig tlConfig, AmcpChannel channel, IControlInterface controlInterface, LinkedList<Trigger> triggers) {
        config = tlConfig;
        this.channel = channel;
        this.controlInterface = controlInterface;
        this.triggers = triggers;

        changeState(TimelineState.READY);
    }

    public int getChannelNumber(){
        return channel.channelId();
    }

    private void changeState(TimelineState newState) {
        if (controlInterface != null)
            controlInterface.notifyState(newState);
    }

    @Override
    public AmcpLayer getLayer(int layerId) {
        AmcpLayer layer = layerCache.get(layerId);

        if (layer == null) {
            layer = new AmcpLayer(channel, layerId);
            layerCache.put(layerId, layer);
        }

        return layer;
    }

    public void stop() {
        System.out.println("Timeline " + config.getId() + " received stop");
        running = false;
    }

    public void kill() {
        System.out.println("Timeline " + config.getId() + " received kill");
        killNow = true;
        running = false;
    }

    @Override
    public void setLayerState(int layerId, LayerState state) {
        currentLayerState.put(layerId, state);
    }

    @Override
    public LayerState getLayerState(int layerId) {
        return currentLayerState.get(layerId);
    }

    @Override
    public CopyOnWriteArrayList<Trigger> getActiveTriggers() {
        return activeTriggers;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isWaitingForCue() {
        return activeTriggers.stream().anyMatch(t -> t.getType() == TriggerType.CUE);
    }

    private boolean isRequiredTemplateDataDefined() {
        ArrayList<String> fields = getTemplateDataFields(triggers);
        for (String fieldName : fields) {
            if (fieldName.indexOf("@") == 0 && !templateData.containsKey(fieldName.substring(1))) {
                changeState(TimelineState.ERROR);
                return false;
            }
        }
        return true;
    }

    public static ArrayList<String> getTemplateDataFields(LinkedList<Trigger> triggers){
        ArrayList<String> fields = new ArrayList<>();

        for (Trigger t : triggers) {
            for (ICommand c : t.getCommands()) {
                fields.addAll(Arrays.asList(c.getTemplateFields()));
            }
        }

        return fields;
    }

    @Override
    public void run() {
        if (running) return;
        running = true;

        // check all template datasets are defined
        if (!isRequiredTemplateDataDefined()) {
            running = false;
            return;
        }

        // run any setup triggers
        long setupTriggerCount = triggers.stream().filter(t -> t.getType() == TriggerType.SETUP).count();
        if (setupTriggerCount > 1){
            System.out.println("Timeline can only have one setup trigger");
            running = false;
            return;
        }

        Trigger setupTrigger = null;
        if (triggers.peekFirst().getType() == TriggerType.SETUP)
            setupTrigger = triggers.pop();

        changeState(TimelineState.RUN);

        System.out.println("Starting timeline " + config.getId());

        // collect the list of channels being altered
        for (Trigger t : triggers) {
            for (ICommand c : t.getCommands()) {
                usedLayers.add(c.getLayerId());
            }
        }

        System.out.println("Template spans " + usedLayers.size() + " layers");

        // set some triggers as active
        promoteTriggersToActive();

        // Run the setup
        if (setupTrigger != null)
            executeTrigger(setupTrigger);

        while (running) {
            synchronized (this) {
                if (triggers.isEmpty() && activeTriggers.isEmpty())
                    break;
            }

            if (isWaitingForCue())
                changeState(TimelineState.CUE);
            else
                changeState(TimelineState.RUN);

            // wait until the timeline has been finished
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // if kill command has been sent, then wipe everything
        if (killNow) {
            changeState(TimelineState.ERROR);
            triggers.clear();
            activeTriggers.clear();
        }

        //ensure everything has been reset
        clearAllUserLayers();

        System.out.println("Finished running timeline");
        running = false;
        changeState(TimelineState.CLEAR);
    }

    private void clearAllUserLayers() {
        for (Integer l : usedLayers) {
            ICommand c = new ClearCommand(l);
            c.execute(this);
            System.out.println("Clearing layer " + l);
        }
    }

    private boolean promoteTriggersToActive() {
        int moved = 0;

        while (!triggers.isEmpty()) {
            Trigger t = triggers.pop();
            moved++;

            activeTriggers.add(t);

            // we only want to add up until a manual trigger
            if (t.getType() == TriggerType.CUE)
                break;
        }

        return moved > 0;
    }

    public synchronized void triggerCue() {
        if (!running) {
            System.err.println("Received cue when not running");
            return;
        }
        System.out.println("Received a cue");
        // TODO - maybe this should be buffered, otherwise there could be some timing issues

        changeState(TimelineState.RUN);

        // find trigger to cue
        Optional<Trigger> waiting = activeTriggers.stream().filter(t -> t.getType() == TriggerType.CUE).findFirst();
        if (!waiting.isPresent()) {
            System.err.println("Received a cue without a trigger to fire");
            return;
        }

        // run the trigger
        executeTrigger(waiting.get());
        activeTriggers.remove(waiting.get());

        if (!promoteTriggersToActive()) {
            System.out.println("Reached end of timeline");
        }
    }

    public synchronized void triggerOnVideoFrame(int layer, long frame, long totalFrames) {
        if (!running) return;

        for (Trigger t: activeTriggers) {
            if (t.getType() == TriggerType.SETUP)
                continue;

            if (t.getLayerId() != layer)
                continue;

            // determine the frame we are aiming for
            long targetFrame = totalFrames;
            if (t.getType() != TriggerType.END) {
                targetFrame = t.getTargetFrame();
            }

            LayerState state = currentLayerState.get(layer);
            if (state == null) {
                System.err.println("Tried to get state and failed");
            }
            // TODO - this check needs to ensure that an appropriate amount of time has passed
            // NOTE: this also gets hit if the source video is a different framerate to the channel
            else if (state.getPreviousFrame() == frame && targetFrame > frame) {
                // the video didn't play to the end for some reason, move on
                System.err.println("Loop didn't reach the end, check your video!");

                executeTrigger(t);
            } else if (t.hasWaited()) {
                // do it
                executeTrigger(t);
            } else if (frame >= targetFrame) {
                t.setWaited();
            }
        }

        if (currentLayerState.containsKey(layer)) {
            currentLayerState.get(layer).setPreviousFrame(frame);
        }
    }

    private void executeTrigger(Trigger trigger) {
        // TODO - may want to look into using a thread to do the sending/commands, as they block until they get a response
        // may cause issues with integrity of triggers lists though
        trigger.getCommands().forEach(c -> c.execute(this));
        activeTriggers.remove(trigger);
    }

    public String getTemplateData(String fieldName) {
        if (fieldName.indexOf("@") == 0)
            return templateData.get(fieldName.substring(1));

        return fieldName;
    }

    public void setTemplateData(HashMap<String, String> templateData) {
        this.templateData = templateData != null ? templateData : new HashMap<>();
    }
}
