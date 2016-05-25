# C-Viz

## Description
C-Viz is a timeline based controller for casparcg, designed to aid in controlling complex linear graphics with ease.

Documentation on the control protocol and timeline file syntax can be found [here](docs)


## Running
The compiled jar file can be run directly with ```java -jar CasparViz.jar```.

Note: Is is recommended to run C-Viz on the same machine as CasparCG, due to the OSC protocol being used which can be sensitive to high latency. If this is not possible, then another machine connected over gigabit networking should suffice.

OSC must be enabled to send to the machine running C-Viz (127.0.0.1 if on the same machine, at port 5253 as defined in TimelineManager.java)

Note: The channel used is currently hardcoded in TimeLinemanager.java, and is set to channel 1 by default. Using more than one channel on the same caspar host has not been tested.

## Building & Dependencies
C-Viz has been developed and tested with Oracle Java 8, but may work on others.

It has primarily been developed and tested on Windows 7 and is highly likely to work on other platforms without issue.

An ant buildfile is provided to provide a simple build process.