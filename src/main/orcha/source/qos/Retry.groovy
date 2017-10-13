package source.qos

title "retry"

receive event from retryInputFile			
compute serviceWithRetry with event.value
when "serviceWithRetry terminates"
send serviceWithRetry.result to qosOutputFile