package cviz.control.tcp;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.net.Socket;

public class ControlClient implements Runnable {
    private final TCPControlState state;
    private final Gson gson = new Gson();

    private Socket socket;

    private final JsonReader inputStream;
    private final JsonWriter outputStream;

    public ControlClient(TCPControlState state, Socket socket) throws IOException {
        this.state = state;
        this.socket = socket;

        try {
            outputStream =  new JsonWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
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

    @Override
    public void run() {
        while (isConnected()) {
            try {
                if(inputStream.hasNext()) {
                    ClientAction action = gson.fromJson(inputStream, ClientAction.class);
                    System.out.println("Received action: " + action);
                    state.runAction(action);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }

            } catch (Exception e) {
                System.err.println("Lost Connection to peer" + e.getMessage());
                close();
            }
        }
    }

    public void close() {
        synchronized (outputStream) {
            try {
                if (socket != null)
                    socket.close();
                System.err.println("Closing Connection");
            } catch (IOException e) {
            }
            socket = null;
        }
    }

    public boolean sendState() {
        if (!isConnected())
            return false;

        new Thread(() -> {
            synchronized (outputStream) {
                try {
                    gson.toJson(state, TCPControlState.class, outputStream);
                    outputStream.flush();
                } catch (IOException ioe) {
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
        }).start();

        return true;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}
