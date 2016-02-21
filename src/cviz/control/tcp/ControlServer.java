package cviz.control.tcp;

import cviz.TimelineManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ControlServer implements Runnable {
    private static final int portNumber = 3456;

    private final TCPControlState state;

    private CopyOnWriteArrayList<ControlClient> clients = new CopyOnWriteArrayList<>();

    private ServerSocket server = null;

    public ControlServer(TCPControlState state){
        this.state = state;
    }

    @Override
    public void run() {
        // check we arent already bound
        if (server != null)
            return;

        System.out.println("Starting Server");

        // try and open the server
        try {
            server = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(10);
        }

        System.out.println("Server started on port: " + portNumber);

        while (server != null) {
            try {
                // try and accept the peer
                Socket socket = server.accept();
                System.out.println("client connected");

                ControlClient client = new ControlClient(state, socket);
                clients.add(client);

                // handle messages from the client
                new Thread(client).start();
                client.sendState();
                System.out.println("client ready");

            } catch (Exception e) {
                System.err.println("Failed to accept client: " + e.getMessage());
            }
        }

        System.out.println("Server stopped");
    }

    /**
     * Close the listener
     */
    public void close() {
        try {
            System.out.println("Stopping Server");
            server.close();
        } catch (IOException e) {
        }
        server = null;
    }

    public void sendState() {
        clients.stream().filter(c -> c.isConnected()).forEach(c -> c.sendState());
    }
}
