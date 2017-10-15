package source.qos

title "queue"
description "Add a queue to the input file reader, in front of a service and to the output file writer."

receive event from queueInputFile			
compute serviceWithQueue with event.value
when "serviceWithQueue terminates"
send serviceWithQueue.result to queueOutputFile