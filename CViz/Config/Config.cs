using System.Linq;

namespace CViz.Config
{
    public class Config
    {
        public int Port { get; set; }
        public int OscPort { get; set; }
        public string CasparHost { get; set; }
        public int CasparPort { get; set; }
        public string TemplateDir { get; set; }

        public ChannelConfig[] Channels { get; set; }

        public ChannelConfig GetChannelById(string id)
        {
            return Channels.FirstOrDefault(c => c.Id == id);
        }
    }
}
