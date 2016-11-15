package cviz.listing;

import cviz.TimelineManager;

import java.io.File;
import java.io.FilenameFilter;

class TimelineFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File directory, String fileName) {
        return fileName.endsWith(TimelineManager.timelineExt);
    }
}
