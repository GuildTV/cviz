package cviz.control.tcp;

import cviz.TimelineManager;
import cviz.TimelineState;
import cviz.config.Config;
import cviz.control.IControlInterface;
import cviz.state.State;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TCPControlInterface implements IControlInterface {

    private final ControlServer server;
    private final ConcurrentLinkedQueue<State> stateChangeQueue;

    public TCPControlInterface(Config config, TimelineManager manager){
        this.stateChangeQueue = new ConcurrentLinkedQueue();

        this.server = new ControlServer(config, manager);
        new Thread(this.server).start();
    }

    @Override
    public void notifyState(State state) {
        stateChangeQueue.add(state);
    }

    @Override
    public void run() {
        while(true){
            State nextState = stateChangeQueue.poll();
            if (nextState != null) {
                server.sendState(nextState);
                continue;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }
}
