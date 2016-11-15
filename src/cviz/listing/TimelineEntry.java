package cviz.listing;

import java.io.Serializable;
import java.util.Arrays;

public class TimelineEntry implements Serializable {
    private final String name;
    private final String[] datasets;

    TimelineEntry(String name, String[] datasets) {
        this.name = name;
        this.datasets = datasets;
    }

    public String getName() {
        return name;
    }

    public String[] getDatasets() {
        return datasets;
    }

    public String toString(){
        return "Timeline: " + getName() + ", datasets: " + Arrays.toString(getDatasets());
    }
}
