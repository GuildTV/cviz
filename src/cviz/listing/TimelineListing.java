package cviz.listing;

import cviz.Timeline;
import cviz.TimelineManager;
import cviz.timeline.Parser;
import cviz.timeline.Trigger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class TimelineListing {
    public static ArrayList<TimelineEntry> Scan() {
        return ScanDir(new File(TimelineManager.timelinePath));
    }

    public static ArrayList<TimelineEntry> ScanDir(File dir) {
        ArrayList<TimelineEntry> timelines = new ArrayList<>();

        System.out.println("Scanning for timelines in " + dir.getAbsolutePath());

        if (!dir.isDirectory() || !dir.exists())
            return timelines;

        File[] files = dir.listFiles(new TimelineFileFilter());
        if (files == null)
            return timelines;

        for (File file : files) {
            if (file.isDirectory()) {
                timelines.addAll(ScanDir(file));
                continue;
            }

            TimelineEntry timeline = tryTimeline(file);
            if (timeline != null)
                timelines.add(timeline);
        }

        return timelines;
    }

    private static TimelineEntry tryTimeline(File file) {
        System.out.println("Trying timeline " + file.getName());
        if (!file.isFile() || !file.exists())
            return null;

        LinkedList<Trigger> triggers = Parser.ParseFile(file.getAbsolutePath());
        if (triggers == null) {
            System.err.println("Failed to parse timeline file: " + file.getName());
            return null;
        }

        HashSet<String> parameters = Timeline.getParameterNames(triggers);

        return new TimelineEntry(file.getName(), parameters.toArray(new String[parameters.size()]));
    }
}
