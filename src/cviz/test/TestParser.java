package cviz.test;

import java.util.LinkedList;

import cviz.timeline.Parser;
import cviz.timeline.Trigger;

public class TestParser {
    public static void main(String[] args) {
        LinkedList<Trigger> commands = Parser.ParseFile("nasta-award.tl");
        for (Trigger t : commands) {
            System.out.println(t.toString());

            t.getCommands().forEach(System.out::println);
        }
    }
}
