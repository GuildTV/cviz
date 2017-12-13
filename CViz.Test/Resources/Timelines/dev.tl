@ {
	100 LOADBG GE2016/BGLOOP2
	100 MIXER FILL 0.52 0 1 1 1 Linear
}
@CUE Show {
    100 PLAY
	100 MIXER FILL 0.156 0 1 1 53 Linear
	
}
@DELAY 8 {
	HTTP POST http://localhost:5000/api/run/0
}

# Wait for animation
@DELAY 50 {
}

@RUNCHILDORCUE change Clear {
	100 MIXER FILL 0.52 0 1 1 53 Linear
}
@DELAY 7 {
	HTTP POST http://localhost:5000/api/run/1
}

# Wait for animation
@DELAY 55 {
}