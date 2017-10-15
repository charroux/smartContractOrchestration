package source.qos
description "Use the circuit breaker pattern. Read all files whose names start with circuitBreakerInputFile at the rate of one file per second. Then pass the content of each file to a service. The service fails the two first times. Then the circuit breaker in opened. The thrid attemps to launch the service occurs before the circuit breaker returns to the half opened state, so the service is no more call."

title "circuit breaker"

receive event from circuitBreakerInputFile			
compute serviceWithCircuitBreaker with event.value
when "serviceWithCircuitBreaker terminates"
send serviceWithCircuitBreaker.result to qosOutputFile