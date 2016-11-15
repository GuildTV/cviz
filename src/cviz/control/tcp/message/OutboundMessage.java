package cviz.control.tcp.message;

import cviz.config.ChannelConfig;
import cviz.listing.TimelineEntry;
import cviz.state.State;

import java.io.Serializable;
import java.util.ArrayList;

public class OutboundMessage implements Serializable {
    private String type;
    private State[] state;
    private String ping;

    private ArrayList<TimelineEntry> timelines;
    private ChannelConfig[] channels;

    public static OutboundMessage CreateState(State state) {
        OutboundMessage msg = new OutboundMessage("state");
        msg.state = new State[]{state};
        return msg;
    }

    public static OutboundMessage CreateState(State[] state) {
        OutboundMessage msg = new OutboundMessage("state");
        msg.state = state;
        return msg;
    }

    public static OutboundMessage CreatePing(String body) {
        OutboundMessage msg = new OutboundMessage("ping");
        msg.ping = body;
        return msg;
    }

    public static OutboundMessage CreateTimelines(ArrayList<TimelineEntry> timelines) {
        OutboundMessage msg = new OutboundMessage("timelines");
        msg.timelines = timelines;
        return msg;
    }

    public static OutboundMessage CreateChannels(ChannelConfig[] channels) {
        OutboundMessage msg = new OutboundMessage("channels");
        msg.channels = channels;
        return msg;
    }

    private OutboundMessage(String type) {
        this.type = type;
    }

    public State[] getState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public String getPing() {
        return ping;
    }

    public ArrayList<TimelineEntry> getTimelines() {
        return timelines;
    }

    public ChannelConfig[] getChannels() {
        return channels;
    }
}
