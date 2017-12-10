using System;
using System.Threading;
using CViz.Util;

namespace CViz.Control.Tcp
{
    class TcpControlInterface : IControlInterface
    {
        private readonly TimelineManager _manager;
        private readonly ControlServer _server;

        public TcpControlInterface(Config.Config config, TimelineManager manager)
        {
            _manager = manager;

            _server = new ControlServer(config, manager);
            new Thread(_server.Run).Start();
        }

        public void NotifyState(State.State state)
        {
            // Nothing to do. state is broadcasted at regular interval without needing a notify
        }

        public void Run()
        {
            while (true)
            {
                _manager.GetCompleteState().ForEach(_server.SendState);

                try
                {
                    Thread.Sleep(10);
                }
                catch (Exception)
                {
                }
            }
        }
    }
}