package orcha.lang.configuration

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Queue {
   long capacity() 		default -1L		
   long fixedDelay() 	default -1L		// in milliseconds the delay between two services invocation (the delay starts after service completion) 
   long fixedRate()		default -1L		// in milliseconds the rate of a service invocation
   String cron()		default ""
}

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Unlike the CircuitBreaker pattern the same message is processed again an again until the maxNumberOfAttempts is reached after which:
 * - the maxNumberOfAttempts is reinitialized to zero
 * - another message is processed
 *
 * In other words, there are maxNumberOfAttempts attempts to solve a problem for a message after which the message is lost.
 *
 * @author Charroux_std
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Retry {
   int maxNumberOfAttempts()						default -1;
   long intervalBetweenTheFirstAndSecondAttempt() 	default -1L;		// in milliseconds
   int intervalMultiplierBetweenAttemps()			default -1;
   long maximumIntervalBetweenAttempts()			default -1L; 	// in milliseconds
   int orderInChain()								default 1;
}


/** 
 * Unlike the Retry pattern, a processed message is never processed again even if a failure occurs: it is lost, but a new one can arrives.
 * If a message arrives while the intervalBeforeHalfOpening it is lost (it won't be processed).
 * During the intervalBeforeHalfOpening even if a single failure occurs again, then the circuit is definitively open (no other message will be processed),
 * while if a successful attempt occurs during the intervalBeforeHalfOpening, the numberOfFailuresBeforeOpening is reinitialized to 0,
 * the circuit is closed again and other message can be processed.   
 * 
 * @author Ben C.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface CircuitBreaker {
   int numberOfFailuresBeforeOpening()		default -1
   long intervalBeforeHalfOpening() 		default -1L		// in millisecondsn
   int orderInChain()						default 1
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EventSourcing {
	
	public enum JoinPoint { before, after, beforeAndAfter }
	
	public enum MessageStore { mongoDB }
	
   String eventName()
   MessageStore messageStore()
   JoinPoint joinPoint()					default JoinPoint.before
   boolean resumeAtStoppingPoint()			default false
}

enum BranchingPosition { after, before }

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface BranchPoint {
   String configurationFile()
   BranchingPosition position()
}
