package source.qos

title "queue"

receive event from queueInputFile			
compute serviceWithQueue with event.value
when "serviceWithQueue terminates"
send serviceWithQueue.result to queueOutputFile