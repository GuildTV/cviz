package cviz.config;

import java.io.Serializable;

public class Config implements Serializable {

    private int port;
    private int oscPort;

    private String casparHost;
    private int casparPort;

    private String templateDir;

    private ChannelConfig[] channels;

    public Config(int port, int oscPort, String casparHost, int casparPort, String templateDir, ChannelConfig[] channels) {
        this.port = port;
        this.oscPort = oscPort;
        this.casparHost = casparHost;
        this.casparPort = casparPort;
        this.templateDir = templateDir;
        this.channels = channels;
    }

    public int getPort() {
        if (port <= 0)
            return 3456;

        return port;
    }

    public int getOscPort() {
        if (oscPort <= 0)
            return 5253;

        return oscPort;
    }

    public String getCasparHost() {
        if (casparHost == null || casparHost.length() == 0)
            return "127.0.0.1";

        return casparHost;
    }

    public int getCasparPort() {
        if (casparPort <= 0)
            return 5250;

        return casparPort;
    }

    public String getTemplateDir(){
        if (templateDir == null || templateDir.equals(""))
            return "./";

        return templateDir;
    }

    public ChannelConfig[] getChannels() {
        if (channels == null)
            return new ChannelConfig[]{new ChannelConfig("default", 1)};

        return channels;
    }

    public ChannelConfig findChannelById(String id) {
        for (ChannelConfig channel : channels) {
            if (channel.getId().equals(id))
                return channel;
        }

        return null;
    }
}
