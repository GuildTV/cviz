using System;
using System.IO;
using System.Reflection;
using System.Threading;
using CViz.Control;
using CViz.Control.Tcp;
using log4net;
using log4net.Config;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Serialization;

namespace CViz
{
    public class CasparViz
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(CasparViz));

        private const int VersionMajor = 0;
        private const int VersionMinor = 5;
        private const int VersionHotfix = 0;

        private static string Version => $"v{VersionMajor}.{VersionMinor}.{VersionHotfix}";

        public static void Main(string[] args)
        {
            var logRepository = LogManager.GetRepository(Assembly.GetEntryAssembly());
            XmlConfigurator.Configure(logRepository, new FileInfo("log4net.config"));
            if (!logRepository.Configured) // Default to all on the console
                BasicConfigurator.Configure(logRepository);

            Log.InfoFormat("Starting CViz {0}", Version);

            JsonConvert.DefaultSettings = (() =>
            {
                var settings = new JsonSerializerSettings();
                settings.Converters.Add(new StringEnumConverter {CamelCaseText = true});
                settings.ContractResolver = new CamelCasePropertyNamesContractResolver();
                return settings;
            });

            Config.Config config;
            try
            {
                config = JsonConvert.DeserializeObject<Config.Config>(File.ReadAllText("./config.json"));

                if (config.Slots.Length == 0)
                    throw new Exception("No channels defined in config file");

            } catch (Exception e)
            {
                Log.ErrorFormat("Failed to load config: {0}", e.Message);
                Console.ReadKey(); // Pause until keypress
                return;
            }

            TimelineManager manager = new TimelineManager(config);

            IControlInterface controlInterface = new TcpControlInterface(config, manager);
            manager.BindInterface(controlInterface);

            new Thread(controlInterface.Run).Start();
            
            Console.ReadKey(); // Pause until keypress
        }
    }
}
