title "event filtering"

receive event from eventFilteringInputFile condition "event == 0"
compute eventFilteringCode with event.value
when "eventFilteringCode terminates"
send eventFilteringCode.result to eventFilteringOutputFile
