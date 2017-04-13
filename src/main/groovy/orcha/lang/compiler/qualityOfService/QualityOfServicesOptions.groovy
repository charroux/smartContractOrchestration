package orcha.lang.compiler.qualityOfService

import groovy.transform.ToString;
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.EventSourcing.JoinPoint

@ToString
class QualityOfServicesOptions {
	def sameEvent
	boolean failTest
	boolean severalWhenWithSameApplicationsInExpression
	boolean autoStartup = true
	QueueOption queue
	RetryOption retry
	CircuitBreakerOption circuitBreaker
	EventSourcingOption eventSourcing
}

@ToString
class QueueOption{
	long capacity = -1
	long fixedDelay = -1
	long fixedRate = -1
	String cron = ""
}

@ToString
class RetryOption{
	int maxNumberOfAttempts = -1
	long intervalBetweenTheFirstAndSecondAttempt = -1L		
	int intervalMultiplierBetwennAttemps = -1
	long maximumIntervalBetweenAttempts = -1L
	int orderInChain
}

@ToString
class CircuitBreakerOption {
   int numberOfFailuresBeforeOpening = -1
   long intervalBeforeHalfOpening = -1L
   int orderInChain	
}

@ToString
class EventSourcingOption {
	String eventName
	//String className
	JoinPoint joinPoint
	boolean resumeAtStoppingPoint
 }
