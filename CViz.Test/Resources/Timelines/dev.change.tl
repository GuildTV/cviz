@ {
  200 LOADBG GE2016/BGLOOP2
  201 CG ADD 0 http://localhost:8080/sidebar.html 0
  200 MIXER FILL 0.52 0 1 1 1 Linear
  201 MIXER FILL 0.52 0 1 1 1 Linear
}

@Q Show {
  200 MIXER FILL 0.156 0 1 1 54 Linear DEFER
  201 MIXER FILL 0.156 0 1 1 54 Linear DEFER

  # Might want a delay 1, to ensure the commands have been run (I cant remember if they run async or sync)
  0 MIXER COMMIT
}

# Wait for animation time + a couple to be safe
@DELAY 56 {
  # Fade out, to make sure the shadow edge cut isnt visible
  # TODO - should shadow be a seperate layer to remove this possible visual glitch?
  100 MIXER OPACITY 0 5 Linear
  101 MIXER OPACITY 0 5 Linear
}

# Wait for fade to finish, then clear
@DELAY 6 {
  100 CLEAR
  101 CLEAR
  100 MIXER CLEAR
  101 MIXER CLEAR
}

# After clear has been done, swap the layers
@DELAY 2 {
  100 SWAP 200 TRANSFORMS
  101 SWAP 201 TRANSFORMS
}
