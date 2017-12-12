using System.Collections.Generic;
using System.IO;
using CViz.Timeline;
using CViz.Timeline.Triggers;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace CViz.Test
{
    [TestClass]
    public class TestTimelines
    {
        [TestMethod]
        public void TestDev()
        {
            LoadTimeline("dev");
        }


        [TestMethod]
        public void TestElections2017CandidateBoard()
        {
            LoadTimeline("elections-2017/candidateBoard");
        }

        [TestMethod]
        public void TestElections2017CandidateNonSabbs()
        {
            LoadTimeline("elections-2017/candidateNonSabbs");
        }

        [TestMethod]
        public void TestElections2017CandidateSabbs()
        {
            LoadTimeline("elections-2017/candidateSabbs");
        }

        [TestMethod]
        public void TestElections2017LowerThird()
        {
            LoadTimeline("elections-2017/lowerThird");
        }

        [TestMethod]
        public void TestElections2017SidebarPhoto()
        {
            LoadTimeline("elections-2017/sidebarPhoto");
        }

        [TestMethod]
        public void TestElections2017SidebarText()
        {
            LoadTimeline("elections-2017/sidebarText");
        }

        [TestMethod]
        public void TestElections2017SplitScreen()
        {
            LoadTimeline("elections-2017/splitScreen");
        }

        [TestMethod]
        public void TestElections2017Winnersall()
        {
            LoadTimeline("elections-2017/winnersAll");
        }

        [TestMethod]
        public void TestElections2017WinnersNonSabbs()
        {
            LoadTimeline("elections-2017/winnersNonSabbs");
        }

        [TestMethod]
        public void TestElections2017WinnersSabbs()
        {
            LoadTimeline("elections-2017/winnersSabbs");
        }



        [TestMethod]
        public void TestXplosion2017Lt()
        {
            LoadTimeline("xplosion-2017/lt");
        }

        [TestMethod]
        public void TestXplosion2017Table()
        {
            LoadTimeline("xplosion-2017/table");
        }


        private void LoadTimeline(string filename)
        {
            List<ITrigger> triggers = Parser.ParseFile(Path.Combine("Resources", "Timelines", filename + TimelineManager.TimelineExt));
            Assert.IsNotNull(triggers);
            Assert.AreNotEqual(0, triggers.Count);
        }
    }
}
