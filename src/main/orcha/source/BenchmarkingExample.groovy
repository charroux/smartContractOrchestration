				

receive event from inputFile	 
compute code1 with event.value
receive event from inputFile
compute code2 with event.value
when "code1 terminates and code2 terminates"
send code1.result to outputFile
