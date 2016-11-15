package cviz.control.tcp;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cviz.TimelineManager;
import cviz.config.ChannelConfig;
import cviz.control.tcp.message.ClientAction;
import cviz.control.tcp.message.InboundCommand;
import cviz.control.tcp.message.OutboundMessage;
import cviz.listing.TimelineEntry;
import cviz.listing.TimelineListing;
import cviz.state.State;

import java.io.*;
import java.net.Socket;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.Arrays;

public class ControlClient implements Runnable {
    private final TimelineManager manager;
    private final Gson gson = new Gson();

    private Socket socket;
    private boolean connected;

    private final JsonReader inputStream;
    private final JsonWriter outputStream;
    private final OutputStream rawOutputStream;

    public ControlClient(TimelineManager manager, Socket socket) throws IOException {
        this.manager = manager;
        this.socket = socket;
        this.connected = true;

        try {
            rawOutputStream = socket.getOutputStream();
            outputStream = new JsonWriter(new OutputStreamWriter(rawOutputStream, "UTF-8"));
            inputStream = new JsonReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            inputStream.setLenient(true);
        } catch (IOException e) {
            System.err.println("Socket failed");
            try {
                socket.close();
            } catch (Exception e1) {
            }

            throw e;
        }
    }

    private void runAction(ClientAction action) {
        switch (action.getType()) {
            case KILL:
                manager.killTimeline(action.getName());
                break;

            case LOAD:
                String instanceId = action.getTemplateDataId() != null ? action.getTemplateDataId() : "";
                if (manager.loadTimeline(action.getChannel(), action.getName(), action.getFilename(), instanceId)) {
                    manager.startTimeline(action.getName(), action.getTemplateData());
                }
                break;

            case CUE:
                manager.triggerCue(action.getName());
                break;

            case QUERY:
                // Nothing to do
                break;

            default:
                System.err.println("Unknown action type: " + action.getType());
                break;
        }
    }

    @Override
    public void run() {
        while (isConnected()) {
            try {
                if (inputStream.hasNext()) {
                    InboundCommand command = gson.fromJson(inputStream, InboundCommand.class);

                    switch (command.getType()) {
                        case Ping:
                            replyPing(command.getPing());
                            break;
                        case Action:
                            performAction(command.getAction());
                            break;
                        case Timelines:
                            sendTimelines();
                            break;
                        case Channels:
                            sendChannels();
                            break;
                        case State:
                            sendState();
                            break;
                        default:
                            System.err.println("Got unknown command type from client: " + command.getType());
                    }

                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }

            } catch (Exception e) {
                System.err.println("Lost Connection to peer " + e.getMessage());
                // e.printStackTrace();
                close();
            }
        }
    }

    private void sendState() {
        State[] state = manager.getCompleteState();
        System.out.println("Found " + state.length + " running timelines");
        OutboundMessage msg = OutboundMessage.CreateState(state);
        sendOutbound(msg);
    }

    private boolean performAction(ClientAction action) {
        if (action != null) {
            if (action.getType() == ClientAction.ActionType.QUERY) {
                System.out.println("Received state query");
                sendState(manager.getStateForTimelineId(action.getName()));
            } else {
                System.out.println("Received action: " + action);
                runAction(action);
            }
            return true;
        }
        return false;
    }

    private void sendTimelines() {
        ArrayList<TimelineEntry> timelines = TimelineListing.ScanDir(new File(TimelineManager.timelinePath));
        System.out.println("Found " + timelines.size() + " timelines");
        OutboundMessage msg = OutboundMessage.CreateTimelines(timelines);
        sendOutbound(msg);
    }

    private void sendChannels() {
        ChannelConfig[] channels = manager.getConfig().getChannels();

        System.out.println("Found " + channels.length + " channels");
        OutboundMessage msg = OutboundMessage.CreateChannels(channels);
        sendOutbound(msg);
    }

    public void close() {
        synchronized (outputStream) {
            connected = false;
            try {
                if (socket != null)
                    socket.close();
                System.err.println("Closing Connection");
            } catch (IOException e) {
            }
            socket = null;
        }
    }

    boolean sendState(State state) {
        if (state == null)
            return true;

        OutboundMessage message = OutboundMessage.CreateState(state);
        return sendOutbound(message);
    }

    private boolean sendOutbound(OutboundMessage msg) {
        if (msg == null)
            return true;

        if (!isConnected())
            return false;

        synchronized (outputStream) {
            try {
                gson.toJson(msg, OutboundMessage.class, outputStream);
                outputStream.flush();
            } catch (JsonIOException je) {
                System.err.println("Failed to send message: " + je.getMessage());
            } catch (Exception ioe) {
                System.err.println("Failed to send message: " + ioe.getMessage());
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
                socket = null;
            }
        }

        return true;
    }

    private boolean replyPing(String msg) {
        if (!isConnected())
            return false;

        new Thread(() -> {
            synchronized (outputStream) {
                OutboundMessage message = OutboundMessage.CreatePing(msg);
                sendOutbound(message);
            }
        }).start();

        return true;
    }

    public boolean isConnected() {
        return socket != null && connected && socket.isConnected();
    }

    void sendCompleteState() {
        State[] state = manager.getCompleteState();

        Arrays.stream(state).forEach(this::sendState);
    }
}
