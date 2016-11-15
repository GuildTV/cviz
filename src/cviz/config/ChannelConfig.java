package cviz.config;

import java.io.Serializable;

public class ChannelConfig implements Serializable {
    private String id;
    private int channel;

    public ChannelConfig(String id, int channel) {
        this.id = id;
        this.channel = channel;
    }

    public String getId() {
        if (id == null || id.length() == 0)
            return "default";

        return id;
    }

    public int getChannel() {
        if (channel <= 0)
            return 1;

        return channel;
    }
}
