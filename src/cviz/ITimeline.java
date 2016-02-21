package cviz;

import cviz.timeline.Trigger;
import se.svt.caspar.amcp.AmcpLayer;

import java.util.concurrent.CopyOnWriteArrayList;

public interface ITimeline extends Runnable {
    int getChannelNumber();

    AmcpLayer getLayer(int layerId);

    void triggerCue();

    void triggerOnVideoFrame(int layer, long frame, long totalFrames);

    boolean isRunning();
    void stop();
    void kill();

    void setLayerState(int layerId, LayerState state);
    LayerState getLayerState(int layerId);

    CopyOnWriteArrayList<Trigger> getActiveTriggers();

    String getTemplateData(String fieldName);
}
