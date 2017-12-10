using System.Collections.Generic;

namespace CViz.Control.Tcp
{
    class ClientAction
    {
        public enum ActionType
        {
            Unknown,
            Load,
            Cue,
            Kill,
            Query
        }

        public string Channel { get; set; }
        public string TimelineSlot { get; set; }
        public ActionType Type { get; set; }
        public string TimelineFile { get; set; }
        public string InstanceName { get; set; }
        public Dictionary<string, string> Parameters { get; set; }

        public ClientAction()
        {
            Channel = "default";
            TimelineSlot = "default";
        }

        public override string ToString()
        {
            string paramCount = Parameters?.Count.ToString() ?? "-";
            return $"Action: {Type} {TimelineFile} data: {InstanceName} {paramCount}";
        }
    }
}
