using LibAtem.Commands.Macro;

namespace CViz.Timeline.Command
{
    class AtemMacroRunCommand : AtemCommandBase
    {
        private readonly uint _macroId;

        public AtemMacroRunCommand(uint macroId)
        {
            _macroId = macroId;
        }

        public override bool Execute(ITimeline timeline)
        {
            timeline.AtemClient.SendCommand(new MacroActionCommand(){Action = MacroActionCommand.MacroAction.Run, Index = _macroId});

            return true;
        }

        public override string ToString()
        {
            return $"Macro {_macroId}";
        }
    }
}