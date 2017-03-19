package cviz.control.tcp;

import cviz.TimelineManager;
import cviz.TimelineState;
import cviz.config.Config;
import cviz.control.IControlInterface;
import cviz.state.State;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TCPControlInterface implements IControlInterface {

    private final ControlServer server;
    private final TimelineManager manager;

    public TCPControlInterface(Config config, TimelineManager manager){
        this.manager = manager;

        this.server = new ControlServer(config, manager);
        new Thread(this.server).start();
    }

    @Override
    public void notifyState(State state) {
        // Nothing to do. state is broadcasted at regular interval without needing a notify
    }

    @Override
    public void run() {
        while(true){
            State[] state = manager.getCompleteState();
            for (State st : state) {
                server.sendState(st);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }
}
