				

receive event1 from inputFile condition "event1 == 0"	  		// pas de when, pas de then (remplacé par ligne au dessous)
compute code1 with event1.value
receive event2 from inputFile condition "event2 == 1"
compute code2 with event2.value
when "code1 terminates and code2 terminates"
send code1.result to outputFile
