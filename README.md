# C-Viz

## Description
C-Viz is a controller for CasparCG, inspired by Viz, that uses timelines to manage complex linear graphics.

Documentation on the control protocol and timeline file syntax can be found [here](docs)

## Running
Before running, a config file must be created. This should be based on config.json, and tweaked to the run environment.

Note: Is is recommended to run C-Viz on the same machine as CasparCG, due to the OSC protocol being used which can be sensitive to high latency. If this is not possible, then another machine connected over gigabit networking should suffice.

OSC must be enabled to send to the machine running C-Viz (127.0.0.1 if on the same machine, at the port defined in config.json)

## Building & Dependencies
C-Viz require .NET Framework 4.6.1 to run. Visual Studio 2017 is required to build the project.

It has been primarily tested on Windows 10, but should run on other versions of Windows without issue.
