title "event filtering"

receive event from eventFilteringInputFile condition "event == 0"
compute eventFilteringCode1 with event.value
when "eventFilteringCode1 terminates"
send eventFilteringCode1.result to eventFilteringOutputFile
