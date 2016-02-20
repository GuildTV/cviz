package cviz.test;

import java.util.Iterator;
import java.util.LinkedList;

import cviz.*;

public class TestParser {
    public static void main(String[] args) {
        LinkedList<Trigger> commands = Parser.Parse("c:\\caspar\\new.tl");
        Iterator<Trigger> i = commands.iterator();
        while(i.hasNext()) {
            Trigger t = i.next();
            System.out.println("TRIGGER: " + t.getType() + " " + t.getTime() + " " + t.getLayer());
            Command c;
            while((c = t.getNextCommand()) != null) {
                System.out.println("\tCOMMAND: " + c.getLayer() + " " + c.getAction() + " " + c.getName());
            }
        }
    }
}
