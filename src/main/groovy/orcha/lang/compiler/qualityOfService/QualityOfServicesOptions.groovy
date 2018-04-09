package orcha.lang.compiler.qualityOfService

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString;
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.EventSourcing.JoinPoint
import orcha.lang.configuration.EventSourcing.MessageStore

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
	int capacity = -1
	long fixedDelay = -1L
	long fixedRate = -1L
	String cron = ""
}

@ToString
class RetryOption{
	int maxNumberOfAttempts = -1
	long intervalBetweenTheFirstAndSecondAttempt = -1L		
	int intervalMultiplierBetweenAttemps = -1
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
	MessageStore messageStore
	String eventName
	//String className
	JoinPoint joinPoint
	boolean resumeAtStoppingPoint
 }
