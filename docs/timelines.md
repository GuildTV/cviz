# Timeline syntax
Some real examples can be found in the [tests](CViz.Test/Resources/Timelines)

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

### Delay
This triggers runs after the specified duration (in frames).

Note: It currently requires a modified CasparCG build, with the following patch applied: https://github.com/Julusian/CasparCG-Server/commit/8dca3dca21488af083960c4d4bf650486ffe77f0

```
# @DELAY duration
@DELAY 10 {
  ...
}
```

### Cue
These are run when the cued by a control interface. Only the first cue at the top of the trigger stack is run when cued. The description is sent back to the client to aid the control software in knowing the position through the timeline.

```
# @Q or @CUE
@CUE Some description here {
  ...
}
```

### Run child or Cue
This trigger has two possible outcomes. If it receives a cue action from the client, then it behaves like the Cue trigger above. Instead, if a runchild action gets called then a child template is executed instead.
The child template must be named the same as the parent, with the child-name from the command specified before the file extension (eg template1.tl becomes template1.change.tl). This must exist when the template is loaded or the load will fail.
The child template can be run any number of times until a cue is received, each time with a different set of data.

One use case of this trigger is to have the main template do an in and out animation, with the child template to change the content. This allows for dynamically changing the template content without having to know many times it will need to be done when designing the timeline

```
# @RUNCHILDORCUE child-name descriptionh
@RUNCHILDORCUE change Some description here {
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
  100 MIXER OPACITY @o1 @o2
  100 PLAY
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
  100 LOADBG bgloop
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

### Mixer Commit
This is a special case handling as the AMCP command does not want a layer specified. Instead it should be prefixed with 0

```
  0 MIXER COMMIT
```

### HTTP Request
This command performs a http request to the specified url. This can be used to trigger other systems to perform an action at a certain point in a timeline

```
  HTTP POST http://localhost/run/1
  HTTP GET http://localhost/run/1
```
