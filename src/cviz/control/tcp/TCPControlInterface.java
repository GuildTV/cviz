package cviz.control.tcp;

import cviz.TimelineManager;
import cviz.TimelineState;
import cviz.config.Config;
import cviz.control.IControlInterface;

public class TCPControlInterface implements IControlInterface {

    private final ControlServer server;
    private final TCPControlState state;

    private TimelineState newState;

    public TCPControlInterface(Config config, TimelineManager manager){
        state = new TCPControlState(manager);
        newState = TimelineState.ERROR;

        this.server = new ControlServer(config, state);
        new Thread(this.server).start();
    }

    @Override
    public void notifyState(TimelineState state) {
        newState = state;
    }

    @Override
    public void run() {
        while(true){
            if(newState != state.getState()){
                state.setState(newState);
                processStateChange();
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

    private void processStateChange(){
        System.out.println("New state " + state);
        server.sendState();
    }
}
