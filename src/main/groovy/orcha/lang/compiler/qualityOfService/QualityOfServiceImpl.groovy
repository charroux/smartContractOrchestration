package orcha.lang.compiler.qualityOfService

import orcha.lang.compiler.InstructionNode;
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.configuration.Retry
import orcha.lang.configuration.EventSourcing.JoinPoint
import orcha.lang.configuration.Queue
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.EventSourcing.MessageStore

class QualityOfServiceImpl implements QualityOfService{
	
	/**
	 * Queue are added to a node from its preceding node because of the receive
	 * instructions already having in their routers channels in which queues are added.
	 *
	 * @param graphOfInstructions
	 * @return
	 */
	void setQualityOfServiceToInstructions(OrchaCodeVisitor orchaCodeParser){
		
		ArrayList<InstructionNode> nodes = orchaCodeParser.findAllNodes()
		def instructionNodeAlreadyDone = []
		
		int nodeIndex = 0
		
		nodes.each{ it ->
			
			if(it.instruction.instruction == "receive"){

				QueueOption optionQueue = this.getQueueOption(it)
				
				if(optionQueue !=  null){
					if(it.options == null){							// a receive node
						it.options = new QualityOfServicesOptions()
					}
					it.options.queue = optionQueue
				}
				
				EventSourcingOption eventSourcingOption = this.getEventSourcingOption(it)
				if(eventSourcingOption !=  null){
					if(it.options == null){
						it.options = new QualityOfServicesOptions()
					}
					it.options.eventSourcing = eventSourcingOption
				}

				if(it.next.instruction.instruction == "receive"){
						
					InstructionNode node = it.next
					int i=0
								
					InstructionNode receive
					InstructionNode nextToReceive
					
					int instructionNumber=0
					
					while(node != null){
									
						boolean set = false
												
						while(instructionNumber<nodes.size() && set==false){
							
							receive = nodes.getAt(instructionNumber)
							
							if(node.instruction == receive.instruction){							// look for receive node
								
								instructionNodeAlreadyDone.add(receive)
															
								if(receive.next != null){
									
									int j=instructionNumber+1
									
									while(j<nodes.size() && set==false){				// look for receive's next node:
										
										nextToReceive = nodes.getAt(j) 				// typically a compute
										
										if(receive.next.instruction == nextToReceive.instruction){
											
											optionQueue = this.getQueueOption(nextToReceive)
											if(optionQueue !=  null){
												
												if(node.options == null){							// a receive node
													node.options = new QualityOfServicesOptions()
												}
												node.options.queue = optionQueue
												
												if(nextToReceive.options == null){					// the next node from receive
													nextToReceive.options = new QualityOfServicesOptions()			// typically a compute (service activator)
												}													// parametrized with a poller: fixedRate...
												nextToReceive.options.queue = optionQueue
											}
											
											eventSourcingOption = this.getEventSourcingOption(it)
											if(eventSourcingOption !=  null){
												
												if(it.options == null){
													it.options = new QualityOfServicesOptions()
												}
												it.options.eventSourcing = eventSourcingOption
												
												if(nextToReceive.options == null){							// the next node from receive
													nextToReceive.options = new QualityOfServicesOptions()	// typically a compute (service activator)
												}													
												nextToReceive.options.eventSourcing = eventSourcingOption
											}
											set = true
										}
										j++
									}
								}
							}
							
							instructionNumber++
						}
							
						node = node.next
						i++
					}
					
				}
				
			} else if(it.instruction.instruction == "compute"){
				
				QueueOption optionQueue = this.getQueueOption(it)
				
				if(optionQueue !=  null){
					if(it.options == null){							// a receive node
						it.options = new QualityOfServicesOptions()
					}
					it.options.queue = optionQueue
				}
				
				RetryOption retryOption = this.getRetryOption(it)
				if(retryOption !=  null){
					if(it.options == null){
						it.options = new QualityOfServicesOptions()
					}
					it.options.retry = retryOption
				}
				
				CircuitBreakerOption circuitBreakerOption = this.getCircuitBreakerOption(it)
				if(circuitBreakerOption !=  null){
					if(it.options == null){
						it.options = new QualityOfServicesOptions()
					}
					it.options.circuitBreaker = circuitBreakerOption
				}
				
				EventSourcingOption eventSourcingOption = this.getEventSourcingOption(it)
				if(eventSourcingOption !=  null){
					if(it.options == null){
						it.options = new QualityOfServicesOptions()
					}
					it.options.eventSourcing = eventSourcingOption
				}
				
			} else if(it.instruction.instruction == "when"){
		
				InstructionNode nextNode
				int i= nodeIndex
				
				while(i<nodes.size() && (nextNode=nodes.getAt(i)).instruction!=it.next.instruction){
					i++
				}
				
				QueueOption optionQueue = this.getQueueOption(nextNode)
				if(optionQueue !=  null){
					
					if(it.options == null){
						it.options = new QualityOfServicesOptions()
					}
					it.options.queue = optionQueue
					
					if(nextNode.options == null){
						nextNode.options = new QualityOfServicesOptions()
					}
					nextNode.options.queue = optionQueue
				}
			} else if(it.instruction.instruction == "send"){
				
				EventSourcingOption eventSourcingOption = this.getEventSourcingOption(it)
				if(eventSourcingOption !=  null){
					if(it.options == null){
						it.options = new QualityOfServicesOptions()
					}
					it.options.eventSourcing = eventSourcingOption
				}
			}
			
			//println "it=" + it
			
			nodeIndex++
		}
	}

	private QueueOption getQueueOption(InstructionNode instructionNode) throws OrchaConfigurationException{
		
		Class<?> beanClass = instructionNode.instruction.springBean.getClass()
		
		if(beanClass.isAnnotationPresent(Queue.class)){
			
			long queueCapacity = beanClass.getAnnotation(Queue.class).capacity()
			if(queueCapacity == -1){
				throw new OrchaConfigurationException("A queue should have a capacity at integration configuration of " + beanClass.name)
			} else if(queueCapacity <= 0){
				throw new OrchaConfigurationException("Queue option queueCapacity should be >= 0 at integration configuration of " + beanClass.name)
			}
			
			long fixedDelay = beanClass.getAnnotation(Queue.class).fixedDelay()
			if(fixedDelay!=-1 && fixedDelay<=0){
				throw new OrchaConfigurationException("Queue option fixedDelay should be >= 0 at integration configuration of " + beanClass.name)
			}
			
			long fixedRate = beanClass.getAnnotation(Queue.class).fixedRate()
			if(fixedRate!=-1 && fixedRate<=0){
				throw new OrchaConfigurationException("Queue option fixedRate should be >= 0 at integration configuration of " + beanClass.name)
			}
			
			String cron = beanClass.getAnnotation(Queue.class).cron()
			
			if(fixedDelay==-1 && fixedRate==-1 && cron==""){
				throw new OrchaConfigurationException("A fixedDelay, fixedRate or cron should be set when a queue is used at integration configuration of " + beanClass.name)
			}
			if(fixedDelay!=-1 && fixedRate!=-1 && cron!=""){
				throw new OrchaConfigurationException("Conflict between fixedDelay, fixedRate and cron while fixing the Queue attributs at integration configuration of " + beanClass.name)
			}
			if(fixedDelay!=-1 && fixedRate!=-1){
				throw new OrchaConfigurationException("Conflict between fixedDelay and fixedRate while fixing the Queue attributs at integration configuration of " + beanClass.name)
			}
			if(fixedDelay!=-1 && cron!=""){
				throw new OrchaConfigurationException("Conflict between fixedDelay and cron while fixing the Queue attributs at integration configuration of " + beanClass.name)
			}
			if(fixedRate!=-1 && cron!=""){
				throw new OrchaConfigurationException("Conflict between fixedRate and cron while fixing the Queue attributs at integration configuration of " + beanClass.name)
			}
			return new QueueOption(capacity: queueCapacity, fixedDelay: fixedDelay, fixedRate: fixedRate, cron: cron)
		} else {
			return null
		}
	}
	
	private RetryOption getRetryOption(InstructionNode instructionNode) throws OrchaConfigurationException{
		
		Class<?> beanClass = instructionNode.instruction.springBean.getClass()
		
		if(beanClass.isAnnotationPresent(Retry.class)){
			
			int maxNumberOfAttempts = beanClass.getAnnotation(Retry.class).maxNumberOfAttempts()
			if(maxNumberOfAttempts <= 0){
				throw new OrchaConfigurationException("Queue option maxNumberOfAttempts should be >= 0 at integration configuration of " + beanClass.name)
			}
			if(maxNumberOfAttempts == -1){
				throw new OrchaConfigurationException("Queue option maxNumberOfAttempts should be set at integration configuration of " + beanClass.name)
			}
			
			long intervalBetweenTheFirstAndSecondAttempt = beanClass.getAnnotation(Retry.class).intervalBetweenTheFirstAndSecondAttempt()
			if(intervalBetweenTheFirstAndSecondAttempt <= 0){
				throw new OrchaConfigurationException("Queue option maxNumberOfAttempts should be >= 0 at integration configuration of " + beanClass.name)
			}
			if(intervalBetweenTheFirstAndSecondAttempt == -1){
				throw new OrchaConfigurationException("Queue option intervalBetweenTheFirstAndSecondAttempt should be set at integration configuration of " + beanClass.name)
			}
			
			int intervalMultiplierBetwennAttemps = beanClass.getAnnotation(Retry.class).intervalMultiplierBetweenAttemps()
			if(intervalMultiplierBetwennAttemps <= 0){
				throw new OrchaConfigurationException("Queue option intervalMultiplierBetwennAttemps should be >= 0 at integration configuration of " + beanClass.name)
			}
			if(intervalBetweenTheFirstAndSecondAttempt == -1){
				throw new OrchaConfigurationException("Queue option intervalMultiplierBetwennAttemps should be set at integration configuration of " + beanClass.name)
			}
			
			int maximumIntervalBetweenAttempts = beanClass.getAnnotation(Retry.class).maximumIntervalBetweenAttempts()
			if(maximumIntervalBetweenAttempts <= 0){
				throw new OrchaConfigurationException("Queue option maximumIntervalBetweenAttempts should be >= 0 at integration configuration of " + beanClass.name)
			}
			if(maximumIntervalBetweenAttempts == -1){
				throw new OrchaConfigurationException("Queue option maximumIntervalBetweenAttempts should be set at integration configuration of " + beanClass.name)
			}
			
			int orderInChain = beanClass.getAnnotation(Retry.class).orderInChain()
			if(orderInChain <= 0){
				throw new OrchaConfigurationException("Queue option orderInChain should be >= 0 at integration configuration of " + beanClass.name)
			}
			
			return new RetryOption(maxNumberOfAttempts: maxNumberOfAttempts, intervalBetweenTheFirstAndSecondAttempt: intervalBetweenTheFirstAndSecondAttempt, intervalMultiplierBetweenAttemps: intervalMultiplierBetwennAttemps, maximumIntervalBetweenAttempts: maximumIntervalBetweenAttempts, orderInChain: orderInChain )
		} else {
			return null
		}
	}
	
	private CircuitBreakerOption getCircuitBreakerOption(InstructionNode instructionNode) throws OrchaConfigurationException{
		
		Class<?> beanClass = instructionNode.instruction.springBean.getClass()
		
		if(beanClass.isAnnotationPresent(CircuitBreaker.class)){
			
			int numberOfFailuresBeforeOpening = beanClass.getAnnotation(CircuitBreaker.class).numberOfFailuresBeforeOpening()
			if(numberOfFailuresBeforeOpening <= 0){
				throw new OrchaConfigurationException("Circuit Breaker option numberOfFailuresBeforeOpening should be >= 0 at integration configuration of " + beanClass.name)
			}
			if(numberOfFailuresBeforeOpening == -1){
				throw new OrchaConfigurationException("Circuit Breaker option numberOfFailuresBeforeOpening should be set at integration configuration of " + beanClass.name)
			}
			
			long intervalBeforeHalfOpening = beanClass.getAnnotation(CircuitBreaker.class).intervalBeforeHalfOpening()
			if(intervalBeforeHalfOpening <= 0){
				throw new OrchaConfigurationException("Circuit Breaker option intervalBeforeHalfOpening should be >= 0 at integration configuration of " + beanClass.name)
			}
			if(intervalBeforeHalfOpening == -1){
				throw new OrchaConfigurationException("Circuit Breaker option intervalBeforeHalfOpening should be set at integration configuration of " + beanClass.name)
			}
			
			int orderInChain = beanClass.getAnnotation(CircuitBreaker.class).orderInChain()
			if(orderInChain <= 0){
				throw new OrchaConfigurationException("Circuit Breaker option orderInChain should be >= 0 at integration configuration of " + beanClass.name)
			}
			
			return new CircuitBreakerOption(numberOfFailuresBeforeOpening: numberOfFailuresBeforeOpening, intervalBeforeHalfOpening: intervalBeforeHalfOpening, orderInChain: orderInChain)
		} else {
			return null
		}
	}
	
	private EventSourcingOption getEventSourcingOption(InstructionNode instructionNode) throws OrchaConfigurationException{
		
		Class<?> beanClass = instructionNode.instruction.springBean.getClass()
		
		if(beanClass.isAnnotationPresent(EventSourcing.class)){
			
			String eventName = beanClass.getAnnotation(EventSourcing.class).eventName()
			//String className = "orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer"
			boolean resumeAtStoppingPoint = beanClass.getAnnotation(EventSourcing.class).resumeAtStoppingPoint()
			JoinPoint joinPoint = beanClass.getAnnotation(EventSourcing.class).joinPoint()
			MessageStore messageStore = beanClass.getAnnotation(EventSourcing.class).messageStore()
				
			//return new EventSourcingOption(eventName: eventName, className: className, resumeAtStoppingPoint: resumeAtStoppingPoint)
			return new EventSourcingOption(messageStore: messageStore, eventName: eventName, joinPoint: joinPoint, resumeAtStoppingPoint: resumeAtStoppingPoint)
			
		} else {
			return null
		}
	}

}
