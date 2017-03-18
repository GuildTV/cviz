package cviz;

import java.net.SocketException;

import cviz.control.ConsoleControlInterface;
import cviz.control.IControlInterface;
import cviz.control.tcp.TCPControlInterface;

public class CasparViz {

	public static void main(String[] args) throws InterruptedException, SocketException {
        System.out.println("Caspar-viz v0.3 running");

		TimelineManager manager = new TimelineManager();

		IControlInterface controlInterface = new TCPControlInterface(manager);
		manager.bindInterface(controlInterface);

		new Thread(controlInterface).start();
	}
}
