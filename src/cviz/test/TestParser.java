package cviz.test;

import java.util.Iterator;
import java.util.LinkedList;

import cviz.timeline.Parser;
import cviz.timeline.Trigger;

public class TestParser {
    public static void main(String[] args) {
        LinkedList<Trigger> commands = Parser.ParseFile("nasta-award.tl");
        Iterator<Trigger> i = commands.iterator();
        while(i.hasNext()) {
            Trigger t = i.next();
            System.out.println(t.toString());

            t.getCommands().forEach(System.out::println);
        }
    }
}
