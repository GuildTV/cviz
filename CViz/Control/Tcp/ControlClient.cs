using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace CViz.Control.Tcp
{
    class ControlClient
    {
        private readonly TimelineManager _manager;
        private Socket _socket;
        private readonly object _sendLock;
        private bool _connected;

        // Size of receive buffer.  
        private const int BufferSize = 4096;
        // Receive buffer.  
        private byte[] _buffer = new byte[BufferSize];

        private StringBuilder _sb = new StringBuilder();

        public ControlClient(TimelineManager manager, Socket socket)
        {
            _manager = manager;
            _socket = socket;
            _connected = true;

            _sendLock = new object();
        }

        public void Run()
        {
            _socket.BeginReceive(_buffer, 0, BufferSize, 0, ReadCallback, null);
        }

        private void ReadCallback(IAsyncResult ar)
        {
            // Read data from the client socket.   
            int bytesRead = _socket.EndReceive(ar);

            if (bytesRead <= 0)
            {
                Console.WriteLine("0 byte message. closing connection");
                _socket.Close();
                return;
            }

            TryParseRemaining(_buffer, 0, bytesRead);

            // There  might be more data, so store the data received so far.  
            //            _sb.Append(Encoding.ASCII.GetString(_buffer, 0, bytesRead));
            //
            //            // Check for end-of-file tag. If it is not there, read   
            //            // more data.  
            //            content = _sb.ToString();
            //            if (content.IndexOf("\n", StringComparison.InvariantCulture) > -1)
            //            {
            //                Console.WriteLine("Read {0} bytes from socket. \n Data : {1}", content.Length, content);
            //                // Echo the data back to the client.  
            ////                    Send(handler, content);
            //            }
            //            else
            //            {
            // Not all data received. Get more.  
            _socket.BeginReceive(_buffer, 0, BufferSize, 0, ReadCallback, null);
            //            }
        }

        private void TryParseRemaining(byte[] arr, int start, int length)
        {
            for (int i = start; i < length; i++)
            {
                if (arr[i] == '\n')
                {
                    int len = i + 1 - start;
                    _sb.Append(Encoding.ASCII.GetString(arr, start, len));
                    string res = _sb.ToString();
                    _sb.Clear();

                    TryParseRemaining(arr, i + 1, length);
                    ParseAndRun(res);
                    Console.WriteLine("Got: " + res);
                }
            }

            // Queue remainder and stop for next time
            _sb.Append(Encoding.ASCII.GetString(arr, start, length - start));
        }

        private void ParseAndRun(string str)
        {
            ClientAction action = JsonConvert.DeserializeObject<ClientAction>(str);
            if (action == null || action.Type == ClientAction.ActionType.Unknown)
            {
                ReplyPing();
            }
            else if (action.Type == ClientAction.ActionType.Query)
            {
                Console.WriteLine("Received state query");
                SendState(_manager.GetStateForTimelineSlot(action.TimelineSlot));
            }
            else
            {
                Console.WriteLine("Received action: " + action);
                RunAction(action);
            }
        }

        private void RunAction(ClientAction action)
        {
            switch (action.Type)
            {
                case ClientAction.ActionType.Kill:
                    _manager.KillTimeline(action.TimelineSlot);
                    break;

                case ClientAction.ActionType.Load:
                    if (_manager.LoadTimeline(action.Channel, action.TimelineSlot, action.TimelineFile, action.InstanceName ?? ""))
                        _manager.StartTimeline(action.TimelineSlot, action.Parameters);
                    break;

                case ClientAction.ActionType.Cue:
                    _manager.TriggerCue(action.TimelineSlot);
                    break;

                case ClientAction.ActionType.Query:
                    // Nothing to do
                    break;

                default:
                    Console.WriteLine("Unknown action type: " + action.Type);
                    break;
            }
        }

        public void Close()
        {
            lock (_sendLock)
            {
                _connected = false;
                try
                {
                    _socket?.Close();
                    Console.WriteLine("Closing Connection");
                }
                catch (IOException)
                {
                }
                _socket = null;
            }
        }

        internal bool SendState(State.State state)
        {
            if (state == null)
                return true;

            if (!IsConnected)
                return false;


            Task.Run(() => SendObject(state));
            return true;
        }

        private void SendObject(object obj)
        {
            lock (_sendLock)
            {
                try
                {
                    _socket.Send(Encoding.ASCII.GetBytes(JsonConvert.SerializeObject(obj)));
                }
                catch (Exception)
                {
                    try
                    {
                        _socket?.Close();
                    }
                    catch (IOException)
                    {
                    }
                    _socket = null;
                }
            }
        }

        private bool ReplyPing()
        {
            if (!IsConnected)
                return false;

            Task.Run(() => SendObject(new object()));
            return true;
        }

        public bool IsConnected => _socket != null && _connected && _socket.Connected;

        internal void SendCompleteState()
        {
            IReadOnlyList<State.State> state = _manager.GetCompleteState();

            foreach (State.State s in state)
                SendState(s);
        }
    }
}