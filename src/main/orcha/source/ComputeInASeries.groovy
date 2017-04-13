				

receive event from inputFile			
compute program1 with event.value
when "program1 terminates"
compute program2 with program1.result
when "program2 terminates"
send program2.result to outputFile
