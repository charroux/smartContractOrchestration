package source.qos

title "retry"
description "Use the retry pattern. The service is automatically launch again 3 times. The two first times the service throws an exception. The third time, the service completes."

receive event from retryInputFile			
compute serviceWithRetry with event.value
when "serviceWithRetry terminates"
send serviceWithRetry.result to qosOutputFile