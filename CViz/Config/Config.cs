using System.Linq;

namespace CViz.Config
{
    public class Config
    {
        public int Port { get; set; }
        public int OscPort { get; set; }

        public string CasparHost { get; set; }
        public int CasparPort { get; set; }

        public string AtemHost { get; set; }

        public string TemplateDir { get; set; }

        public SlotConfig[] Slots { get; set; }

        public SlotConfig GetChannelById(string id)
        {
            return Slots.FirstOrDefault(c => c.Id == id);
        }
    }
}
