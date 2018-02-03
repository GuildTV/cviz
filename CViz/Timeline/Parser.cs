using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;
using CViz.Timeline.Command;
using CViz.Timeline.Triggers;
using log4net;

namespace CViz.Timeline
{
    class Parser
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(Parser));

        private const string SetupTriggerPattern = "^@ \\{$";
        private const string CueTriggerPattern = "^@(Q|CUE) (.*) \\{$";
        private const string RunChildOrCueTriggerPattern = "^@RUNCHILDORCUE ([^\\s]*) (.*) \\{$";
        private const string EndTriggerPattern = "^@END ([0-9]+) \\{$";
        private const string FrameTriggerPattern = "^@([0-9]+) ([0-9]+) \\{$";
        private const string DelayTriggerPattern = "^@DELAY ([0-9]+) \\{$";
        private const string SceneStopTriggerPattern = "^@SCENESTOP ([0-9]+) (.+) \\{$";

        private const string LoadCommandPattern = "^(LOAD|LOADBG) ([\"].+?[\"]|[^ ]+)";
        private const string StopCommandPattern = "^STOP";
        private const string LoopCommandPattern = "^LOOP";
        private const string ClearCommandPattern = "^CLEAR";
        private const string MixerCommitCommandPattern = "^MIXER COMMIT";

        private List<ITrigger> _triggers = new List<ITrigger>();
        private ITrigger _currentTrigger;

        private readonly StreamReader _reader;

        private Parser(StreamReader reader)
        {
            _reader = reader;
        }

        private IReadOnlyList<ITrigger> Parse()
        {
            try
            {
                string line;
                while ((line = _reader.ReadLine()) != null)
                    ParseLine(line);

                return _triggers;
            }
            catch (IOException)
            {
                _triggers = null;
                throw;
            }
            catch (Exception e)
            {
                Log.ErrorFormat("Parser error: {0}", e.Message);
                Log.ErrorFormat(e.StackTrace);
                _triggers = null;
                throw;
            }
        }

        private void ParseLine(string line)
        {
            line = line.Trim();

            if (line.StartsWith("@"))
            {
                // trigger line
                _currentTrigger = ParseTrigger(line);
                if (_currentTrigger == null)
                    throw new Exception("Failed to parse trigger: " + line);
            }
            else if (line.StartsWith("}") && _currentTrigger != null)
            {
                _triggers.Add(_currentTrigger);
                _currentTrigger = null;
            }
            else if (line.StartsWith("#") || line.Length == 0)
            {
                // comment or blank line
            }
            else
            {
                if (_currentTrigger == null)
                    throw new Exception("Failed to add command outside of trigger: " + line);

                _currentTrigger = _currentTrigger.WithCommand(ParseCommand(line));
            }
        }

        private static ITrigger ParseTrigger(String line)
        {
            Match matcher = Regex.Match(line, SetupTriggerPattern);
            if (matcher.Success)
                return new SetupTrigger();

            matcher = Regex.Match(line, CueTriggerPattern);
            if (matcher.Success)
                return new CueTrigger(matcher.Groups[2].Value);

            matcher = Regex.Match(line, RunChildOrCueTriggerPattern);
            if (matcher.Success)
                return new RunChildOrCueTrigger(matcher.Groups[1].Value, matcher.Groups[2].Value);

            matcher = Regex.Match(line, EndTriggerPattern);
            if (matcher.Success)
                return new EndTrigger(int.Parse(matcher.Groups[1].Value));

            matcher = Regex.Match(line, FrameTriggerPattern);
            if (matcher.Success)
                return new FrameTrigger(int.Parse(matcher.Groups[2].Value), long.Parse(matcher.Groups[1].Value));
            
            matcher = Regex.Match(line, DelayTriggerPattern);
            if (matcher.Success)
                return new DelayTrigger(int.Parse(matcher.Groups[1].Value));

            matcher = Regex.Match(line, SceneStopTriggerPattern);
            if (matcher.Success)
                return new SceneStopTrigger(int.Parse(matcher.Groups[1].Value), matcher.Groups[2].Value);

            return null;
        }

        private static CommandBase ParseCommand(string line)
        {
            string[] parts = line.Split(new []{' '}, 2);
            if (parts[0] == "HTTP")
                return ParseHttpCommand(parts[1]);
            if (parts[0] == "ATEM")
                return ParseAtemCommand(parts[1]);

            int layerId = int.Parse(parts[0]);
            string command = parts[1];

            Match matcher = Regex.Match(command, LoadCommandPattern);
            if (matcher.Success)
                return new LoadCommand(layerId, command, matcher.Groups[2].Value);

            matcher = Regex.Match(command, StopCommandPattern);
            if (matcher.Success)
                return new StopCommand(layerId, command);

            matcher = Regex.Match(command, LoopCommandPattern);
            if (matcher.Success)
                return new LoopCommand(layerId);

            matcher = Regex.Match(command, ClearCommandPattern);
            if (matcher.Success)
                return new ClearCommand(layerId);

            matcher = Regex.Match(command, MixerCommitCommandPattern);
            if (matcher.Success)
                return new MixerCommitCommand(layerId);

            return new AmcpCommand(layerId, command);
        }

        private static HttpCommand ParseHttpCommand(string line)
        {
            string[] parts = line.Split(' ');
            return new HttpCommand(parts[0], parts[1]);
        }

        private static AtemCommandBase ParseAtemCommand(string line)
        {
            string[] parts = line.Split(' ');
            if (parts[0] == "MACRO" && parts[1] == "RUN")
            {
                uint macroId = uint.Parse(parts[2]);
                return new AtemMacroRunCommand(macroId);
            }

            throw new Exception("Unknown Atem command");
        }

        public static TimelineSpec ParseFile(string basePath, string name)
        {
            try
            {
                string mainPath = Path.Combine(basePath, name + TimelineManager.TimelineExt);
                IReadOnlyList<ITrigger> mainTimeline;
                using (var reader = new StreamReader(mainPath))
                {
                    Parser parser = new Parser(reader);
                    mainTimeline = parser.Parse();
                }

                if (mainTimeline.OfType<SetupTrigger>().Count() > 1)
                    throw new Exception("Timeline can only have one setup trigger");

                IReadOnlyList<string> childNames = mainTimeline.OfType<RunChildOrCueTrigger>().Select(c => c.TimelineName).Distinct().ToList();
                Dictionary<string, IEnumerable<ITrigger>> childTimelines = childNames.ToDictionary(n => n, n =>
                {
                    string childPath = Path.Combine(basePath, $"{name}.{n}{TimelineManager.TimelineExt}");
                    using (var reader = new StreamReader(childPath))
                    {
                        Parser parser = new Parser(reader);
                        IReadOnlyList<ITrigger> triggers = parser.Parse();

                        if (triggers.Any(t => t is RunChildOrCueTrigger))
                            throw new Exception("Cannot have nexted child timelines");

                        if (triggers.OfType<SetupTrigger>().Count() > 1)
                            throw new Exception("Timeline can only have one setup trigger");

                        return triggers.AsEnumerable();
                    }
                });

                return new TimelineSpec(name, mainTimeline, childTimelines);
            }
            catch (IOException e)
            {
                Log.ErrorFormat("Parser error: {0}", e.Message);
                Log.ErrorFormat(e.StackTrace);
                throw;
            }
        }
    }
}
