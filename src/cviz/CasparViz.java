package cviz;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.SocketException;

import com.google.gson.Gson;
import cviz.config.Config;
import cviz.control.ConsoleControlInterface;
import cviz.control.IControlInterface;
import cviz.control.tcp.TCPControlInterface;

public class CasparViz {
    public static final int VersionMajor = 0;
    public static final int VersionMinor = 2;
    public static final int VersionHotfix = 0;

    public static String GetVersion() {
        return "v" + VersionMajor + "." + VersionMinor + "." + VersionHotfix;
    }

    public static void main(String[] args) throws InterruptedException, SocketException {
        System.out.println("Caspar-viz " + GetVersion() + " running");

        Gson gson = new Gson();
        Config config;
        try {
            config = gson.fromJson(new FileReader("config.json"), Config.class);

            if (config.getChannels().length == 0)
                throw new Exception("No channels defined in config file");
        } catch (Exception e) {
            System.err.println("Failed to open config file: " + e.getMessage());
            return;
        }

        TimelineManager manager = new TimelineManager(config);

        IControlInterface controlInterface = new TCPControlInterface(config, manager);
        manager.bindInterface(controlInterface);

        new Thread(controlInterface).start();
    }
}
