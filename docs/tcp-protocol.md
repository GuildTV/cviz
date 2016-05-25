# TCP Protocol
A TCP server is run on port 3456, to be controlled from another device or system, such as [cviz-ui](https://github.com/GuildTV/cviz-ui)

All communication is sent json encoded, for maximum compatability

## Action
Actions take on the following json structure.

```
{
  "type": "", // Command type
  "filename": "", // Template name
  "templateDataId": "", // Name for the data to use when sending state back to clients
  "templateData": {} // Data available to pass to templates
}
```

### Load
This action loads a template and starts running it.

```
{
  "type": "LOAD",
  "filename": "lowerthird2",
  "templateDataId": "bob kelso",
  "templateData": {
    "data": {
      "f0": "Bob Kelso",
      "f1": "Chief of Medicine"
    }
  }
}
```

### Cue
This action requires no data parameters, and is used to run a cue trigger in the timeline

```
{
  "type": "CUE",
  "filename": "",
  "templateDataId": "",
  "templateData": {}
}
```

### Kill
This action requires no data parameters, and is used to abort a template

```
{
  "type": "KILL",
  "filename": "",
  "templateDataId": "",
  "templateData": {}
}
```

## Events
These are send to all connected clients.

### Error
This is fired when a timeline load is failed, caused by a data id being used that was not defined in the templateData set passed in the action to run the timeline.

```
{
  "state": "ERROR",
  "templateName": "",
  "dataId": ""
}
```

### Ready
This is fired once a timeline has been loaded, before it starts running.

```
{
  "state": "READY",
  "templateName": "lowerthird2",
  "dataId": "bob kelso"
}
```

### Cue
This is fired when a timeline is waiting to be cued by a client.

```
{
  "state": "CUE",
  "templateName": "lowerthird2",
  "dataId": "bob kelso"
}
```

### Run
This is fired when the timeline is running.

```
{
  "state": "RUN",
  "templateName": "lowerthird2",
  "dataId": "bob kelso"
}
```

### Clear
Fired once a timeline has finished running, to indicate the system is ready for a new timeline to be loaded

```
{
  "state": "CLEAER",
  "templateName": "",
  "dataId": ""
}
```