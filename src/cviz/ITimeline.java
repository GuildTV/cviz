package cviz;

import cviz.timeline.Trigger;
import se.svt.caspar.amcp.AmcpLayer;

import java.util.concurrent.CopyOnWriteArrayList;

public interface ITimeline {
    AmcpLayer getLayer(int layerId);

    void setLayerState(int layerId, LayerState state);
    LayerState getLayerState(int layerId);

    CopyOnWriteArrayList<Trigger> getActiveTriggers();

    String getTemplateData(String fieldName);
}
