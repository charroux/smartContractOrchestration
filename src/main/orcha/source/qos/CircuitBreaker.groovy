package source.qos

title "circuit breaker"

receive event from circuitBreakerInputFile			
compute serviceWithCircuitBreaker with event.value
when "serviceWithCircuitBreaker terminates"
send serviceWithCircuitBreaker.result to qosOutputFile