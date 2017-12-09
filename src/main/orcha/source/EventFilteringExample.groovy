title "event filtering"
description "Read a text file. Continue only if the content of the file is 0. Then pass 0 as an argument to a service. Wait the service to complete. Then write the result of the service to a file."

receive event from eventFilteringInputFile condition "event.equals('0')"
compute eventFilteringCode with event.value
when "eventFilteringCode terminates"
send eventFilteringCode.result to eventFilteringOutputFile
