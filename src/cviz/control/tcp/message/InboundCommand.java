package cviz.control.tcp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class InboundCommand implements Serializable {
    public enum Types {
        @SerializedName("action")
        Action,
        @SerializedName("ping")
        Ping,
        @SerializedName("timelines")
        Timelines,
        @SerializedName("channels")
        Channels,
    }

    private Types type;
    private ClientAction action;
    private String ping;

    public InboundCommand(Types type, ClientAction action, String ping) {
        this.type = type;
        this.action = action;
        this.ping = ping;
    }
    public Types getType() {
        return type;
    }

    public ClientAction getAction() {
        return action;
    }

    public String getPing() {
        return ping;
    }
}
