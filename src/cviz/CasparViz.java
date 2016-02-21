package cviz;

import java.net.SocketException;

import cviz.control.ConsoleControlInterface;
import cviz.control.IControlInterface;

public class CasparViz {

	public static void main(String[] args) throws InterruptedException, SocketException {
        System.out.println("Caspar-viz v0.1 running");

		TimelineManager manager = new TimelineManager();

		IControlInterface controlInterface = new ConsoleControlInterface(manager);
		manager.bindInterface(controlInterface);

		new Thread(controlInterface).start();
	}
}
