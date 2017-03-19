# Timeline syntax
An example which loads a video and plays it when cued is as follows.
```
# run setup
@ {
  100 LOADBG GE2015/BG_START
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

Note: putting an end trigger on a looped video may cause the trigger to fire only on the end of the first play.

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

All of the Caspar CG AMCP commands are supported, but must be written in the following format, with the layer number first and channel omitted.
```
  100 CLEAR
```

The commands support the use of runtime variables for any command parameter, to be passed in from the client.
```
  100 OPACITY @o1 @o2
  100 PLAY @v1
```

### Clear
Clears the layer of the playing video or template.
Note: This clears very aggressively, by clearing the layer and resetting any opacity and transformations applied.

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
This is a modified play command to loop the loaded video in a reliable and predictable fashion.

Note: a video must have been loaded in the layer before running this command.

```
  100 LOOP
```
