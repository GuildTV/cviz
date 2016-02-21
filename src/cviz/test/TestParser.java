package cviz.test;

import java.util.Iterator;
import java.util.LinkedList;

import cviz.timeline.Command;
import cviz.timeline.Parser;
import cviz.timeline.Trigger;

public class TestParser {
    public static void main(String[] args) {
        LinkedList<Trigger> commands = Parser.Parse("c:\\caspar\\new.tl");
        Iterator<Trigger> i = commands.iterator();
        while(i.hasNext()) {
            Trigger t = i.next();
            System.out.println("TRIGGER: " + t.getType() + " " + t.getTargetFrame() + " " + t.getLayerId());

            for (Command c: t.getCommands()){
                System.out.println("\tCOMMAND: " + c.getLayerId() + " " + c.getAction() + " " + c.getName());
            }
        }
    }
}
