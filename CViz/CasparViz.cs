using System;
using System.IO;
using System.Threading;
using CViz.Control;
using CViz.Control.Tcp;
using Newtonsoft.Json;

namespace CViz
{
    public class CasparViz
    {
        private const int VersionMajor = 0;
        private const int VersionMinor = 5;
        private const int VersionHotfix = 0;

        private static string GetVersion() => $"v{VersionMajor}.{VersionMinor}.{VersionHotfix}";

        public static void Main(string[] args)
        {
            Console.WriteLine("CViz " + GetVersion() + " running");

            Config.Config config;
            try
            {
                config = JsonConvert.DeserializeObject<Config.Config>(File.ReadAllText("./config.json"));

                if (config.Channels.Length == 0)
                    throw new Exception("No channels defined in config file");

            } catch (Exception e) {
                Console.WriteLine("Failed to open config file: " + e.Message);
                return;
            }

            TimelineManager manager = new TimelineManager(config);

            IControlInterface controlInterface = new TcpControlInterface(config, manager);
            manager.BindInterface(controlInterface);

            new Thread(controlInterface.Run).Start();

            Console.WriteLine("Press any key to terminate...");
            Console.ReadKey(); // Pause until keypress

        }
    }
}
