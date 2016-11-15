package cviz.test;

import java.io.File;
import java.util.ArrayList;

import cviz.listing.TimelineEntry;
import cviz.listing.TimelineListing;

public class TestListing {
    public static void main(String[] args) {
        ArrayList<TimelineEntry> timelines = TimelineListing.ScanDir(new File("/home/julus/Projects/cviz/"));

        System.out.println("Found " + timelines.size() + " valid timelines");

        for (TimelineEntry entry : timelines) {
            System.out.println(entry.toString());
        }
    }
}
