using System;
using System.Collections.Immutable;
using System.IO;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using CViz.Timeline;
using CViz.Util;
using log4net;
using Newtonsoft.Json;

namespace CViz.Control.Tcp
{
    class ControlClient
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(ControlClient));

        private readonly TimelineManager _manager;
        private Socket _socket;
        private readonly object _sendLock;
        private bool _connected;
        
        private const int BufferSize = 4096;
        private readonly byte[] _buffer = new byte[BufferSize];
        private readonly StringBuilder _sb = new StringBuilder();

        public ControlClient(TimelineManager manager, Socket socket)
        {
            _manager = manager;
            _socket = socket;
            _connected = true;

            _sendLock = new object();
        }

        public void Run()
        {
            Log.InfoFormat("Starting receive for client: {0}", _socket.RemoteEndPoint);
            _socket.BeginReceive(_buffer, 0, BufferSize, 0, ReadCallback, null);
        }

        private void ReadCallback(IAsyncResult ar)
        {
            try
            {
                // Read data from the client socket.   
                int bytesRead = _socket.EndReceive(ar);

                if (bytesRead <= 0)
                {
                    Log.InfoFormat("Got 0 byte message. Closing connection: {0}", _socket.RemoteEndPoint);
                    _socket.Close();
                    return;
                }

                TryParseRemaining(_buffer, 0, bytesRead);

                _socket.BeginReceive(_buffer, 0, BufferSize, 0, ReadCallback, null);
            }
            catch (Exception e)
            {
                Log.ErrorFormat("Receive error: {0}", e.Message);
            }
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
                    return;
                }
            }

            // Queue remainder and stop for next time
            if (length - start > 0)
                _sb.Append(Encoding.ASCII.GetString(arr, start, length - start));
        }

        private void ParseAndRun(string str)
        {
            try
            {
                ClientAction action = JsonConvert.DeserializeObject<ClientAction>(str.Trim('\n'));
                if (action == null || action.Type == ClientAction.ActionType.Unknown)
                {
                    ReplyPing();
                }
                else if (action.Type == ClientAction.ActionType.Query)
                {
                    Log.InfoFormat("Received state query");
                    SendState(_manager.GetStateForTimelineSlot(action.TimelineSlot));
                }
                else
                {
                    Log.InfoFormat("Received action: {0}", action);
                    RunAction(action);
                }
            }
            catch (Exception e)
            {
                Log.ErrorFormat("Action run error: {0}", e.Message);
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
                    if (_manager.LoadTimeline(action.TimelineSlot, action.TimelineFile, action.InstanceName ?? ""))
                        _manager.StartTimeline(action.TimelineSlot, action.Parameters.ToImmutableDictionary());
                    break;

                case ClientAction.ActionType.Cue:
                    _manager.TriggerCue(action.TimelineSlot);
                    break;

                case ClientAction.ActionType.RunChild:
                    _manager.TriggerChild(action.TimelineSlot, action.InstanceName, action.Parameters);
                    break;

                case ClientAction.ActionType.Query:
                    // Nothing to do
                    break;

                default:
                    Log.ErrorFormat("Unknown action type: {0}", action.Type);
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
                    Log.InfoFormat("Closing client connection");
                }
                catch (IOException)
                {
                }
                _socket = null;
            }
        }

        internal bool SendState(TimelineState state)
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
            _manager.GetCompleteState().ForEach(s => SendState(s));
        }
    }
}