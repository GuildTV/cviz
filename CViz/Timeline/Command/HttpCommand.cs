using System.Collections.Generic;
using System.Net.Http;
using log4net;

namespace CViz.Timeline.Command
{
    class HttpCommand : CommandBase
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(AmcpCommand));
        private static readonly HttpClient Client = new HttpClient();

        private readonly string _method;
        private readonly string _url;

        public HttpCommand(string method, string url) : base(0)
        {
            _method = method;
            _url = url;
        }

        public override bool Execute(ITimeline timeline)
        {
            switch (_method.ToUpper())
            {
                case "POST":
                    var content = new FormUrlEncodedContent(new Dictionary<string, string>());

                    Log.InfoFormat("Http Post: {0}", _url);
                    Client.PostAsync(_url, content);
                    break;
                case "GET":
                    Log.InfoFormat("Http Get: {0}", _url);
                    Client.GetStringAsync(_url);
                    break;
                default:
                    Log.ErrorFormat("Unknown http method: {0}", _method);
                    return false;
            }

            return true;
        }

        public override string ToString()
        {
            return $"Http {_method}: {_url}";
        }
    }
}