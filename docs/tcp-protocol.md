# TCP Protocol
A TCP server is run on port 3456, to be controlled from another device or system, such as [cviz-ui](https://github.com/GuildTV/cviz-ui)

All communication is sent json encoded, for maximum compatability

## Action
Actions take on the following json structure.

```
{
  "type": "", // Command type
  "timelineFile": "", // Template name
  "instanceName": "", // Name for the data to use when sending state back to clients
  "parameters": {} // Data available to pass to templates
}
```

### Load
This action loads a template and starts running it.

```
{
  "type": "load",
  "timelineFile": "lowerthird2",
  "instanceName": "bob kelso",
  "parameters": {
    "data": {
      "f0": "Bob Kelso",
      "f1": "Chief of Medicine"
    }
  }
}
```

### Cue
This action requires no data parameters, and is used to run a cue trigger in the timeline.

```
{
  "type": "cue",
  "timelineFile": "",
  "instanceName": "",
  "parameters": {}
}
```

### Run Child
This action can optionally take data parameters, and is used to run a child timeline, when the timeline is waiting at a RUNCHILDORCUE trigger.

```
{
  "type": "runchild",
  "timelineFile": "",
  "instanceName": "",
  "parameters": {}
}
```

Note: After this action has been processed the instanceName reported by cviz will change to the new instanceName specified. 

### Kill
This action requires no data parameters, and is used to abort a template.

```
{
  "type": "kill",
  "timelineFile": "",
  "instanceName": "",
  "parameters": {}
}
```

## State
These are send to all connected clients.

### Error
This is sent whilst a timeline load is failed, caused by a data id being used that was not defined in the parameters set passed in the action to run the timeline.

```
{
  "state": "error",
  "stateMessage": "An unknown error occured",
  "timelineSlot": "default",
  "timelineFile": "",
  "instanceName": ""
}
```

### Ready
This is fired once a timeline has been loaded, before it starts running.

```
{
  "state": "ready",
  "stateMessage": "",
  "timelineSlot": "default",
  "timelineFile": "lowerthird2",
  "instanceName": "bob kelso"
}
```

### Cue
This is sent whilst a timeline is waiting to be cued by a client.

```
{
  "state": "cue",
  "stateMessage": "start running",
  "timelineSlot": "default",
  "timelineFile": "lowerthird2",
  "instanceName": "bob kelso"
}
```

### Run child or Cue
This is sent whilst a timeline is waiting to be cued by a client or to have the child timeline run.

```
{
  "state": "runchildorcue",
  "stateMessage": "continue or child",
  "timelineSlot": "default",
  "timelineFile": "lowerthird2",
  "instanceName": "bob kelso"
}
```

### Run
This is sent whilst the timeline is running.

```
{
  "state": "run",
  "stateMessage": "",
  "timelineSlot": "default",
  "timelineFile": "lowerthird2",
  "instanceName": "bob kelso"
}
```

### Clear
Sent after a timeline has finished running, to indicate the system is ready for a new timeline to be loaded
Note that the timelineSFile and instanceName fields are still defined, to indicate the timeline that has finished running.

```
{
  "state": "clear",
  "stateMessage": "",
  "timelineSlot": "default",
  "timelineFile": "lowerthird2",
  "instanceName": "bob kelso"
}
```