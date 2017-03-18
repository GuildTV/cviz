# Timeline syntax
An example which loads a video and plays it when cued is as follows.
```
# run setup
@ {
  100 LOAD GE2015/BG_START
}
# play on cue
@Q {
  100 PLAY
}
# wait for the end
@END 100 {
  100 CLEAR
}
```
Note: timelines are determined to be finished once the last trigger has been run. They are cleared quite aggressively (clear, opacity and transform reset) to ensure the state is ready for the next timeline.

Important: blank lines are not supported by the parser

## Triggers
Timeline files are built with triggers, containing commands. There are a few types of triggers, to allow for automated flow with some manual control.


### Setup
There can be only one setup trigger, which must be located at the beginning of the file (if present).
It is run immediately when the timeline is loaded, and should be used to perform some initial setup.

```
@ {
  ...
}
```

### Frame
These are run when the specified layer reaches a certain frame.

```
# @frame layer {
@10 100 {
  ...
}
```

### End
These are run when the video in a layer reaches the end.

Note: putting an end trigger on a looped video may cause the trigger to fire on the end of the first play.

```
# @END layer {
@END 100 {
  ...
}
```

### Cue
These are run when the cued by a control interface. Only the first cue at the top of the trigger stack is run when cued.

```
@Q {
  ...
}
```

## Commands
All commands must be placed inside trigger blocks.

Each command corresponds with a casparcg command, although with slightly different parameter order.

### Clear
Clears the layer of the playing video or template.

```
  100 CLEAR
```

### Load
Loads the specified video file into a layer.

Note: videos must be loaded before running play.

```
  100 LOAD bgloop
```

### Play
Plays the video loaded into the specified layer.

Note: a video must have been loaded in the layer before running this command.

```
  100 PLAY
```

### Loop
Plays the video loaded into the specified layer in a loop.

Note: a video must have been loaded in the layer before running this command.

```
  100 LOOP
```

### Stop
Stops the video playing in the specified layer.

```
  100 STOP
```

### Pause
Pauses the video playing in the specified layer.

```
  100 PAUSE
```

### Resume
Resumes the video playing in the specified layer.

```
  100 RESUME
```

### Opacity
Changes the opacity of a layer. This relays parameters directly to casparcg, documentation an be found on their wiki: [CasparCG-Opacity](http://casparcg.com/wiki/CasparCG_2.0_AMCP_Protocol#MIXER_OPACITY)

```
  100 OPACITY 0 
```

### Transform
Transforms a layer. This relays parameters directly to casparcg, documentation an be found on their wiki: [CasparCG-Transform](http://casparcg.com/wiki/CasparCG_2.0_AMCP_Protocol#MIXER_FILL)

```
  100 TRANSFORM 0 0 100 100
```

### CgAdd
Loads a flash/html template into a layer.

If the data parameter starts with an @, it is used as an id in the dataset passed when starting the timeline, otherwise the value is passed directly through.

Note: templates are set to not autostart when loaded

```
  100 CGADD lowerthird2 @data
```

### CgNext
Fires a next trigger to the flash/html template in the layer.

```
  100 CGNEXT
```

### CgPlay
Play an already loaded template

```
  100 CGPLAY
```

### CgRemove
Removes a template loaded into a layer

```
  100 CGREMOVE
```
  
### CgStop
Fires the stop trigger for the flash/html template in the layer.

```
  100 CGTSOP
```
