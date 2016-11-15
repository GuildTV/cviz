package cviz.control.tcp;

import cviz.TimelineManager;
import cviz.config.Config;
import cviz.state.State;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ControlServer implements Runnable {
    private final Config config;
    private final TimelineManager manager;

    private CopyOnWriteArrayList<ControlClient> clients = new CopyOnWriteArrayList<>();

    private ServerSocket server = null;

    public ControlServer(Config config, TimelineManager manager) {
        this.config = config;
        this.manager = manager;
    }

    @Override
    public void run() {
        // check we arent already bound
        if (server != null)
            return;

        System.out.println("Starting Server");

        // try and open the server
        try {
            server = new ServerSocket(config.getPort());
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(10);
        }

        System.out.println("Server started on port: " + config.getPort());

        while (server != null) {
            try {
                // try and accept the peer
                Socket socket = server.accept();
                System.out.println("client connected");

                ControlClient client = new ControlClient(manager, socket);
                clients.add(client);

                // handle messages from the client
                new Thread(client).start();
                client.sendCompleteState();
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

    public void sendState(State state) {
        clients.stream().filter(c -> c.isConnected()).forEach(c -> c.sendState(state));
    }
}
