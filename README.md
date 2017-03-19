# C-Viz

## Description
C-Viz is a controller for CasparCG, inspired by Viz, that uses timelines to manage complex linear graphics.

Documentation on the control protocol and timeline file syntax can be found [here](docs)


## Running
Before running, a config file must be created. This should be based on config.json.example, and tweaked to the run environment.
The compiled jar file can be run directly with ```java -jar CasparViz.jar```.

Note: Is is recommended to run C-Viz on the same machine as CasparCG, due to the OSC protocol being used which can be sensitive to high latency. If this is not possible, then another machine connected over gigabit networking should suffice.

OSC must be enabled to send to the machine running C-Viz (127.0.0.1 if on the same machine, at the port defined in config.json)

## Building & Dependencies
C-Viz has been developed and tested with Oracle Java 8, but may work on others.

It has primarily been developed and tested on Windows 7 and Debian 8 and is highly likely to work on other platforms without issue.

An ant buildfile is included to provide a simple build process.
