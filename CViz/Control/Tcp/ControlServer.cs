using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using CViz.Timeline;
using CViz.Util;
using log4net;

namespace CViz.Control.Tcp
{
    class ControlServer
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(ControlServer));

        private readonly Config.Config _config;
        private readonly TimelineManager _manager;
        private readonly List<ControlClient> _clients = new List<ControlClient>();

        private readonly ManualResetEvent _allDone = new ManualResetEvent(false);
        private Socket _server;

        public ControlServer(Config.Config config, TimelineManager manager)
        {
            _config = config;
            _manager = manager;
        }


        public void Run()
        {
            // check we arent already bound
            if (_server != null)
                return;

            Log.InfoFormat("Starting TCP Control Server");

            // try and open the server
            try
            {
                IPEndPoint localEndPoint = new IPEndPoint(IPAddress.Any, _config.Port);
                _server = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

                _server.Bind(localEndPoint);
                _server.Listen(100);

                Log.InfoFormat("Listening on port: {0}", _config.Port);

                while (true)
                {
                    // Set the event to nonsignaled state.  
                    _allDone.Reset();

                    // Start an asynchronous socket to listen for connections. 
                    _server.BeginAccept(AcceptCallback, _server);

                    // Wait until a connection is made before continuing.  
                    _allDone.WaitOne();
                }
            }
            catch (IOException e)
            {
                Log.ErrorFormat("Server error: {0}", e.Message);
                Environment.Exit(10);
            }

//            Console.WriteLine("Server stopped");
        }

        private void AcceptCallback(IAsyncResult ar)
        {
            // Signal the main thread to continue.  
            _allDone.Set();

            // Get the socket that handles the client request.  
            Socket listener = (Socket)ar.AsyncState;
            Socket handler = listener.EndAccept(ar);

            Log.InfoFormat("Client connected: {0}", handler.RemoteEndPoint);

            ControlClient client = new ControlClient(_manager, handler);
            lock (_clients)
                _clients.Add(client);
            
            // handle messages from the client
            new Thread(client.Run).Start();
            client.SendCompleteState();
        }


        /**
         * Close the listener
         */
        public void Close()
        {
            try
            {
                Log.InfoFormat("Stopping Server");
                _server.Close();
            }
            catch (IOException)
            {
            }
            _server = null;
        }

        public void SendState(TimelineState state)
        {
            lock (_clients)
                _clients.Where(c => c.IsConnected).ForEach(c => c.SendState(state));
        }
    }
}
