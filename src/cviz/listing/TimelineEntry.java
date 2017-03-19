package cviz.listing;

import java.io.Serializable;
import java.util.Arrays;

public class TimelineEntry implements Serializable {
    private final String name;
    private final String[] parameters;

    TimelineEntry(String name, String[] parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String[] getParameters() {
        return parameters;
    }

    public String toString(){
        return "Timeline: " + getName() + ", parameters: " + Arrays.toString(getParameters());
    }
}
