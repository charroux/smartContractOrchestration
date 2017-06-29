package orcha.lang.compiler.referenceimpl.xmlgenerator.connectors

import java.util.List;

import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.qualityOfService.QualityOfServicesOptions
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.MailReceiverAdapter
import orcha.lang.configuration.MailSenderAdapter
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.EventSourcing.JoinPoint
import orcha.lang.configuration.EventSourcing.MessageStore
import org.springframework.http.MediaType

class SpringIntegrationConnectors{
	
	def inputFileAdapter(InstructionNode instructionNode){
		
		Instruction instruction = instructionNode.instruction
		
		def inputChannel = instructionNode.inputName
		
		/*long queueCapacity = -1
		
		Class<?> eventHandlerClass = instructionNode.instruction.springBean.getClass()
		if(eventHandlerClass.isAnnotationPresent(Queue.class)){
			queueCapacity = eventHandlerClass.getAnnotation(Queue.class).capacity()
		}*/
				
		def id = "headers['id'].toString()"
		def clos = {
	
			if(instructionNode.options!=null && instructionNode.options.queue!=null){
			
				if(instruction.springBean.input.adapter.filenamePattern != null){
					"int-file:inbound-channel-adapter"(id:"file-"+inputChannel+"-id", directory:instruction.springBean.input.adapter.directory, channel:inputChannel, "prevent-duplicates":"true", "filename-pattern":instruction.springBean.input.adapter.filenamePattern, "queue-size":instructionNode.options.queue.capacity){
						
						if(instructionNode.options.queue.fixedDelay != -1){
							"int:poller"("fixed-delay":instructionNode.options.queue.fixedDelay){ }
						} else if(instructionNode.options.queue.fixedRate != -1){
							"int:poller"("fixed-rate":instructionNode.options.queue.fixedRate){ }
						} else if(instructionNode.options.queue.cron != ""){
							"int:poller"("cron":instructionNode.options.queue.cron){ }
						}
					}
				} else {					
					"int-file:inbound-channel-adapter"(id:"file-"+inputChannel+"-id", directory:instruction.springBean.input.adapter.directory, channel:inputChannel, "prevent-duplicates":"true", "queue-size":instructionNode.options.queue.capacity){
						
						if(instructionNode.options.queue.fixedDelay != -1){
							"int:poller"("fixed-delay":instructionNode.options.queue.fixedDelay){ }
						} else if(instructionNode.options.queue.fixedRate != -1){
							"int:poller"("fixed-rate":instructionNode.options.queue.fixedRate){ }
						} else if(instructionNode.options.queue.cron != ""){
							"int:poller"("cron":instructionNode.options.queue.cron){ }
						}
					}
				}
				
				
			} else {
			
				if(instruction.springBean.input.adapter.filenamePattern != null){
					"int-file:inbound-channel-adapter"(id:"file-"+inputChannel+"-id", directory:instruction.springBean.input.adapter.directory, channel:inputChannel, "prevent-duplicates":"true", "filename-pattern":instruction.springBean.input.adapter.filenamePattern){
						"int:poller"(id:"poller-"+inputChannel+"-id", "fixed-delay":"1000"){
						}
					}
				} else {
					"int-file:inbound-channel-adapter"(id:"file-"+inputChannel+"-id", directory:instruction.springBean.input.adapter.directory, channel:inputChannel, "prevent-duplicates":"true"){
						"int:poller"(id:"poller-"+inputChannel+"-id", "fixed-delay":"1000"){
						}
					}
				}

			}

			"int-file:file-to-string-transformer"("input-channel":inputChannel, "output-channel":inputChannel+"Transformer", "delete-files":"false"){
			}
			
			String[] typeAndSubtype = instruction.springBean.input.mimeType.split("/");

			if(typeAndSubtype.length != 2){
				throw new OrchaCompilationException("Unknown Mime Type:" + instruction.springBean.input.mimeType)
			}
			
			def headerEnricherID = "header-enricher-" + inputChannel + "-id"
		
			String condition = null
			
			if(instructionNode.next.instruction.instruction!="receive" && instructionNode.instruction.condition!=null){
				condition = instructionNode.instruction.condition
				condition = condition.replaceFirst(instructionNode.instruction.variable, "payload")
			}
			
			MediaType mediaType = new  MediaType(typeAndSubtype[0], typeAndSubtype[1])
			
			if(mediaType.equals(MediaType.APPLICATION_JSON)){
			
				"int:chain"("input-channel":inputChannel+"Transformer", "output-channel":instructionNode.outputName){
					
					"int:json-to-object-transformer"(type:instruction.springBean.input.type){  }
					
					if(condition != null){
						
						"int:header-enricher"(id:headerEnricherID){
							"int:header"(name:"messageID", expression:id){ }
						}
						
						"int:filter"(expression:condition){
						}
							
					} else {
					
						"int:header-enricher"(id:headerEnricherID){
							"int:header"(name:"messageID", expression:id){ }
						}
					}
				
				}
				
/*				"int:json-to-object-transformer"("input-channel":inputChannel+"Transformer", "output-channel":inputChannel+"JsonTransformer", type:instruction.springBean.input.type){  }
				
				if(condition != null){
					
					"int:header-enricher"(id:headerEnricherID, "input-channel":inputChannel+"JsonTransformer", "output-channel":inputChannel+"HeaderEnricher"){
						"int:header"(name:"messageID", expression:id){ }
					}
					
					"int:filter"("input-channel":inputChannel+"HeaderEnricher", "output-channel":instructionNode.outputName, expression:condition){
					}
						
				} else {
				
					"int:header-enricher"(id:headerEnricherID, "input-channel":inputChannel+"JsonTransformer", "output-channel":instructionNode.outputName){
						"int:header"(name:"messageID", expression:id){ }
					}
				}
*/						
			} else {
				
				"int:chain"("input-channel":inputChannel+"Transformer", "output-channel":instructionNode.outputName){
					
					if(condition != null){
						
						"int:header-enricher"(id:headerEnricherID){
							"int:header"(name:"messageID", expression:id){ }
						}
						
						"int:filter"(expression:condition){
						}
						
					} else {
					
						"int:header-enricher"(id:headerEnricherID){
							"int:header"(name:"messageID", expression:id){ }
						}
					}
	
				}
			
/*				if(condition != null){
					
					"int:header-enricher"(id:headerEnricherID, "input-channel":inputChannel+"Transformer", "output-channel":inputChannel+"HeaderEnricher"){
						"int:header"(name:"messageID", expression:id){ }
					}
					
					"int:filter"("input-channel":inputChannel+"HeaderEnricher", "output-channel":instructionNode.outputName, expression:condition){
					}
					
				} else {
				
					"int:header-enricher"(id:headerEnricherID, "input-channel":inputChannel+"Transformer", "output-channel":instructionNode.outputName){
						"int:header"(name:"messageID", expression:id){ }
					}
				}							
*/				
			}		   
		}
	}

/*	def essai = { InstructionNode instructionNode ->
		
		boolean optionSet = false
		
		if(instructionNode.options!=null && (instructionNode.options.retry!=null || instructionNode.options.circuitBreaker!=null || (instructionNode.options.eventSourcing!=null && (instructionNode.options.eventSourcing.joinPoint==JoinPoint.before || instructionNode.options.eventSourcing.joinPoint == JoinPoint.beforeAndAfter)))){
			
			"int:request-handler-advice-chain"(){
				
				if(instructionNode.options.retry!=null && instructionNode.options.circuitBreaker!=null){
					
					optionSet = true
					
					if(instructionNode.options.retry.orderInChain < instructionNode.options.circuitBreaker.orderInChain){
						"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
							"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
						}
						"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
							"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
							"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
						}
					} else {
						"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
							"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
							"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
						}
						"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
							"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
						}
					}
					
				} else if(instructionNode.options.retry!=null){
					"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
						"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
					}
				}
				
				if(optionSet==false && instructionNode.options!=null && instructionNode.options.circuitBreaker!=null){
						

					if(instructionNode.options.retry!=null){
						
						optionSet = true
						
						if(instructionNode.options.retry.orderInChain < instructionNode.options.circuitBreaker.orderInChain){
							"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
								"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
							}
							"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
								"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
								"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
							}
						} else {
							"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
								"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
								"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
							}
							"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
								"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
							}
						}
						
					} else {
						"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
							"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
							"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
						}
					}
				}
				
				if(instructionNode.options.eventSourcing!=null){
					if(instructionNode.options.eventSourcing.joinPoint == JoinPoint.before){
						"ref"(bean:"eventSourcingAdvice"){ }
					}
				}
				
			}
		}
	}*/	
	
		def serviceActivator(InstructionNode instructionNode, boolean computeFails, String failChannel, boolean isScript){
		
		Instruction instruction = instructionNode.instruction
		
		def applicationName = instruction.springBean.name
		def outputServiceChannel = applicationName + "ServiceAcivatorOutput"
		def id = applicationName + 'Channel'
		def transformerBean = applicationName + "TransformerBean"
		def transformerRef = applicationName
		
		def outputTransformerChannel = outputServiceChannel + "TransformerChannel"
		
		def errorExpression = '@errorUnwrapper.transform(payload)' 
		
		def clos = {  
		
			"int:chain"(id:"service-activator-chain-"+id+"-id", "input-channel":instructionNode.inputName, "output-channel":outputServiceChannel){
		
				if(computeFails == true){
					"int:header-enricher"(){
						"int:error-channel"(ref:failChannel+"-id"){ }
					}
				}
				
				
				if(isScript == false){							

					JavaServiceAdapter javaServiceAdapter = (JavaServiceAdapter)instruction.springBean.input.adapter
					String className = javaServiceAdapter.javaClass
					className = className.substring(className.lastIndexOf('.')+1)
					className = className.substring(0,1).toLowerCase() + className.substring(1)
					def methodName = instruction.springBean.input.adapter.method
					def expression = '@' + className + '.' + methodName + '(payload)'
			
					"int:service-activator"(id:"service-activator-"+id+"-id", expression:expression){
						
						if(instructionNode.options!=null && instructionNode.options.queue!=null){
							if(instructionNode.options.queue.fixedDelay != -1){
							"int:poller"("fixed-delay":instructionNode.options.queue.fixedDelay){ }
							} else if(instructionNode.options.queue.fixedRate != -1){
							"int:poller"("fixed-rate":instructionNode.options.queue.fixedRate){ }
							} else if(instructionNode.options.queue.cron != ""){
							"int:poller"("cron":instructionNode.options.queue.cron){ }
							}
						}
						
						boolean optionSet = false
						
						if(instructionNode.options!=null && (instructionNode.options.retry!=null || instructionNode.options.circuitBreaker!=null || (instructionNode.options.eventSourcing!=null && (instructionNode.options.eventSourcing.joinPoint==JoinPoint.before || instructionNode.options.eventSourcing.joinPoint == JoinPoint.beforeAndAfter)))){
						
						"int:request-handler-advice-chain"(){
						
						if(instructionNode.options.retry!=null && instructionNode.options.circuitBreaker!=null){
							
							optionSet = true
							
							if(instructionNode.options.retry.orderInChain < instructionNode.options.circuitBreaker.orderInChain){
								"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
									"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
								}
								"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
									"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
									"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
								}
							} else {
								"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
									"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
									"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
								}
								"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
									"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
								}
							}
							
						} else if(instructionNode.options.retry!=null){
							"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
								"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
							}
						}
						
						if(optionSet==false && instructionNode.options!=null && instructionNode.options.circuitBreaker!=null){
								
						
							if(instructionNode.options.retry!=null){
								
								optionSet = true
								
								if(instructionNode.options.retry.orderInChain < instructionNode.options.circuitBreaker.orderInChain){
									"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
										"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
									}
									"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
										"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
										"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
									}
								} else {
									"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
										"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
										"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
									}
									"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
										"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
									}
								}
								
							} else {
								"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
									"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
									"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
								}
							}
						}
						
						if(instructionNode.options.eventSourcing!=null){
								if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.before || instructionNode.options.eventSourcing.joinPoint==JoinPoint.beforeAndAfter){
									"ref"(bean:"eventSourcingAdvice"){ }
								}
							}
						
						}
						}
					
					}
					
				} else {
				
					ScriptServiceAdapter scriptingServiceAdapter = (ScriptServiceAdapter)instructionNode.instruction.springBean.input.adapter
					def location = scriptingServiceAdapter.file
					def language = instructionNode.instruction.springBean.language

					"int:service-activator"(id:"service-activator-"+id+"-id"){
						
						"int-script:script"(lang:language, location:location){
						}
						
						if(instructionNode.options!=null && instructionNode.options.queue!=null){
							if(instructionNode.options.queue.fixedDelay != -1){
							"int:poller"("fixed-delay":instructionNode.options.queue.fixedDelay){ }
							} else if(instructionNode.options.queue.fixedRate != -1){
							"int:poller"("fixed-rate":instructionNode.options.queue.fixedRate){ }
							} else if(instructionNode.options.queue.cron != ""){
							"int:poller"("cron":instructionNode.options.queue.cron){ }
							}
						}
						
						boolean optionSet = false
						
						if(instructionNode.options!=null && (instructionNode.options.retry!=null || instructionNode.options.circuitBreaker!=null || (instructionNode.options.eventSourcing!=null && (instructionNode.options.eventSourcing.joinPoint==JoinPoint.before || instructionNode.options.eventSourcing.joinPoint == JoinPoint.beforeAndAfter)))){
						
						"int:request-handler-advice-chain"(){
						
							if(instructionNode.options.retry!=null && instructionNode.options.circuitBreaker!=null){
								
								optionSet = true
								
								if(instructionNode.options.retry.orderInChain < instructionNode.options.circuitBreaker.orderInChain){
									"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
										"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
									}
									"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
										"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
										"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
									}
								} else {
									"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
										"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
										"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
									}
									"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
										"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
									}
								}
								
							} else if(instructionNode.options.retry!=null){
								"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
									"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
								}
							}
							
							if(optionSet==false && instructionNode.options!=null && instructionNode.options.circuitBreaker!=null){
									
							
								if(instructionNode.options.retry!=null){
									
									optionSet = true
									
									if(instructionNode.options.retry.orderInChain < instructionNode.options.circuitBreaker.orderInChain){
										"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
											"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
										}
										"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
											"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
											"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
										}
									} else {
										"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
											"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
											"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
										}
										"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
											"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
										}
									}
									
								} else {
									"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
										"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
										"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
									}
								}
							}
							
							if(instructionNode.options.eventSourcing!=null){
								if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.before || instructionNode.options.eventSourcing.joinPoint==JoinPoint.beforeAndAfter){
									"ref"(bean:"eventSourcingAdvice"){ }
								}
							}
							
							}
						}
					
					}
			
				}

				
			}

			"int:transformer"(id:"transformer-"+outputServiceChannel+"-id", "input-channel":outputServiceChannel, "output-channel":instructionNode.outputName, ref:transformerBean, "method":"transform"){ 
								
				if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){					
					if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.after || instructionNode.options.eventSourcing.joinPoint==JoinPoint.beforeAndAfter){
						"int:request-handler-advice-chain"(){
							"ref"(bean:"eventSourcingAdvice"){ }
						}						
					}
				}
				
			}
			
			"bean"(id:transformerBean, class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer"){
				"property"(name:"application", ref:transformerRef){ }
			}
			
		}

		 
	}
	
/*	def transformerScript(InstructionNode instructionNode, boolean computeFails, String failChannel){
		
		ScriptServiceAdapter scriptingServiceAdapter = (ScriptServiceAdapter)instructionNode.instruction.springBean.input.adapter
		def location = scriptingServiceAdapter.file
		def language = instructionNode.instruction.springBean.language
		
		def applicationName = instructionNode.instruction.springBean.name
		def outputServiceChannel = applicationName + "Output"
		def id = applicationName + 'Channel'
		
		def inputChannel = instructionNode.inputName
		def outputChannel = instructionNode.outputName
		
		def transformerBean = applicationName + "TransformerBean"
		def transformerRef = applicationName
		
		def clos = {
		
			"int:chain"(id:"scripting-"+id+"-id", "input-channel":inputChannel, "output-channel":outputServiceChannel){
				
						if(computeFails == true){
							"int:header-enricher"(){
								"int:error-channel"(ref:failChannel+"-id"){ }
							}
						}
						
						//"int:transformer"(id:"scripting-"+id+"-id", "input-channel":inputChannel, "output-channel":outputServiceChannel){
						"int:transformer"(id:"scripting-"+id+"-id"){
							"int-script:script"(lang:language, location:location){
							}
							if(instructionNode.options!=null && instructionNode.options.queue!=null){
								if(instructionNode.options.queue.fixedDelay != -1){
									"int:poller"("fixed-delay":instructionNode.options.queue.fixedDelay){ }
								} else if(instructionNode.options.queue.fixedRate != -1){
									"int:poller"("fixed-rate":instructionNode.options.queue.fixedRate){ }
								} else if(instructionNode.options.queue.cron != ""){
									"int:poller"("cron":instructionNode.options.queue.cron){ }
								}
							}
							
							boolean optionSet = false
							
												if(instructionNode.options!=null && (instructionNode.options.retry!=null || instructionNode.options.circuitBreaker!=null || (instructionNode.options.eventSourcing!=null && (instructionNode.options.eventSourcing.joinPoint==JoinPoint.before || instructionNode.options.eventSourcing.joinPoint == JoinPoint.beforeAndAfter)))){
													
													"int:request-handler-advice-chain"(){
														
														if(instructionNode.options.retry!=null && instructionNode.options.circuitBreaker!=null){
															
															optionSet = true
															
															if(instructionNode.options.retry.orderInChain < instructionNode.options.circuitBreaker.orderInChain){
																"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
																	"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
																}
																"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
																	"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
																	"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
																}
															} else {
																"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
																	"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
																	"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
																}
																"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
																	"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
																}
															}
															
														} else if(instructionNode.options.retry!=null){
															"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
																"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
															}
														}
														
														if(optionSet==false && instructionNode.options!=null && instructionNode.options.circuitBreaker!=null){
																
										
															if(instructionNode.options.retry!=null){
																
																optionSet = true
																
																if(instructionNode.options.retry.orderInChain < instructionNode.options.circuitBreaker.orderInChain){
																	"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
																		"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
																	}
																	"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
																		"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
																		"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
																	}
																} else {
																	"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
																		"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
																		"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
																	}
																	"int:retry-advice"("max-attempts":instructionNode.options.retry.maxNumberOfAttempts, "recovery-channel":"recoveryChannel"){
																		"int:exponential-back-off"(initial:instructionNode.options.retry.intervalBetweenTheFirstAndSecondAttempt, multiplier: instructionNode.options.retry.intervalMultiplierBetwennAttemps, maximum: instructionNode.options.retry.maximumIntervalBetweenAttempts){ }
																	}
																}
																
															} else {
																"bean"(class:"org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice"){
																	"property"(name:"threshold", value:instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening){ }
																	"property"(name:"halfOpenAfter", value:instructionNode.options.circuitBreaker.intervalBeforeHalfOpening){ }
																}
															}
														}
														
														if(instructionNode.options.eventSourcing!=null){
															if(instructionNode.options.eventSourcing.joinPoint == JoinPoint.before){
																"ref"(bean:"eventSourcingAdvice"){ }
															}
														}
														
													}
												}
							

						}
					 
			}
			
			
			"int:transformer"(id:"transformer-"+outputServiceChannel+"-id", "input-channel":outputServiceChannel, "output-channel":outputChannel, ref:transformerBean, "method":"transform"){ }
			
			"bean"(id:transformerBean, class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer"){
				"property"(name:"application", ref:transformerRef){ }
			}
				
		}
	}*/
	
	def mailSenderAdapter(InstructionNode instructionNode){
		
		def inputChannel = instructionNode.inputName
		
		MailSenderAdapter mailSenderAdapter
		
		if(instructionNode.instruction.springBean.output.adapter instanceof MailSenderAdapter){
			mailSenderAdapter = (MailSenderAdapter)instructionNode.instruction.springBean.output.adapter
		} else if(instructionNode.instruction.springBean.input.adapter instanceof MailSenderAdapter){
			mailSenderAdapter = (MailSenderAdapter)instructionNode.instruction.springBean.input.adapter
			
		}
		
		String to = "@" + instructionNode.instruction.springBean.name + ".input.adapter.to"
		String subject = "@" + instructionNode.instruction.springBean.name + ".input.adapter.subject"
		
		def clos = {
					
			/*"int:object-to-json-transformer"(id:inputChannel+"JSonTransformer-id", "input-channel":inputChannel, "output-channel":inputChannel+"JSonTransformer"){				
			}*/
			
			"int:chain"(id:inputChannel+"JSonTransformer-id", "input-channel":inputChannel, "output-channel":inputChannel+"JSonTransformer"){
				
				if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){
					if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.before){
						"int:transformer"(expression:"payload"){
							"int:request-handler-advice-chain"(){
								"ref"(bean:"eventSourcingAdvice"){ }
							}
						}
					}
				}
					
				"int:object-to-json-transformer"(){ }
				
			}
			
			if(mailSenderAdapter.sendAsAttachmentFile == true){
				"int:transformer"(id:inputChannel+"BytesTransformer-id", "input-channel":inputChannel+"JSonTransformer", "output-channel":inputChannel+"BytesTransformer", expression:"payload.getBytes()"){
				}
				"int-mail:header-enricher"(id:inputChannel+"MailHeaderEnricher-id", "input-channel":inputChannel+"BytesTransformer", "output-channel":inputChannel+"MailHeaderEnricher", "default-overwrite":"false"){
					//"int-mail:to"(value:mailSenderAdapter.to){
					"int-mail:to"(expression:to){
					}
					//"int-mail:subject"(value:mailSenderAdapter.subject){
					"int-mail:subject"(expression:subject){
					}
					"int-mail:attachment-filename"(value:mailSenderAdapter.attachmentFilename){
					}
				}
			} else {
				"int-mail:header-enricher"(id:inputChannel+"MailHeaderEnricher-id", "input-channel":inputChannel+"JSonTransformer", "output-channel":inputChannel+"MailHeaderEnricher", "default-overwrite":"false"){
					"int-mail:to"(value:mailSenderAdapter.to){
					}
					"int-mail:subject"(value:mailSenderAdapter.subject){
					}
					"int-mail:attachment-filename"(value:mailSenderAdapter.attachmentFilename){
					}
				}
			}
			
			
						
			"int:channel"(id:inputChannel+"MailHeaderEnricher"){
			}
			
			"int-mail:outbound-channel-adapter"(id:inputChannel+"outbound-channel-mailSender-id", channel:inputChannel+"MailHeaderEnricher", "mail-sender":inputChannel+"mailSender"){
			}
		
			"bean"(id:inputChannel+"mailSender", class:"org.springframework.mail.javamail.JavaMailSenderImpl"){
				"property"(name:"host", value:mailSenderAdapter.host){					
				}
				"property"(name:"port", value:mailSenderAdapter.port){					
				}
				"property"(name:"username", value:mailSenderAdapter.username){					
				}
				"property"(name:"password", value:mailSenderAdapter.password){					
				}
				"property"(name:"javaMailProperties"){
					"props"(){
						"prop"(key:"mail.smtp.starttls.enable", "true")						
						"prop"(key:"mail.smtp.auth", "true"	)						
						"prop"(key:"mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")						
					}
				}
				   
			}	
		}
	}

	def mailReceiverAdapter(InstructionNode instructionNode){
		
		MailReceiverAdapter mailReceiverAdapter
		def inputChannel
		def typeForConversion
		
		if(instructionNode.instruction.springBean.output!=null && instructionNode.instruction.springBean.output.adapter instanceof MailReceiverAdapter){
			mailReceiverAdapter = (MailReceiverAdapter)instructionNode.instruction.springBean.output.adapter
			inputChannel = instructionNode.inputName + "Out"
			typeForConversion = instructionNode.instruction.springBean.output.type
		} else if(instructionNode.instruction.springBean.input.adapter instanceof MailReceiverAdapter){
			mailReceiverAdapter = (MailReceiverAdapter)instructionNode.instruction.springBean.input.adapter
			inputChannel = instructionNode.inputName
			typeForConversion = instructionNode.instruction.springBean.input.type
		}		
		
		
		def outputChannel = instructionNode.outputName
		
		String username = mailReceiverAdapter.username
		username = username.replace('@', '%40')
		
		String uri = "imaps://" + username + ":" + mailReceiverAdapter.password + "@imap.gmail.com:" + mailReceiverAdapter.port + "/inbox"
		
		//String mailFilterExpression = "subject matches '(?i).*" + mailReceiverAdapter.subjectForFilteringEMails +  ".*'"
		String mailFilterExpression = mailReceiverAdapter.subjectForFilteringEMails
		
		
		def clos = {
			
			"int-mail:imap-idle-channel-adapter"(id:inputChannel+"-EMailImapAdapter-id", "store-uri": uri, channel: inputChannel, "auto-startup":"true", "should-delete-messages":"false", "should-mark-messages-as-read":"false", "java-mail-properties":inputChannel+"javaMailProperties", "mail-filter-expression": mailFilterExpression){				
			}
		
			"util:properties"(id:inputChannel+"javaMailProperties"){
				"prop"(key:"mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
				"prop"(key:"mail.imap.socketFactory.fallback", "false")
				"prop"(key:"mail.store.protocol", "imaps")
				"prop"(key:"mail.debug", "false")
			}	
			
			"int:channel"(id:inputChannel){
			}
			
			"int:chain"(id:inputChannel+ "-EMail-transform-split-id", "input-channel":inputChannel, "output-channel":inputChannel+"EmailTransformer"){
				"int:transformer"(){
					"bean"(class:"orcha.lang.compiler.referenceimpl.util.EmailTransformer"){						
					}
				}
				"int:splitter"(){
					"bean"(class:"orcha.lang.compiler.referenceimpl.util.EmailSplitter"){						
					}
				}
			}
			
			"int:channel"(id:inputChannel+"EmailTransformer"){				
			}
			
			"int-file:outbound-channel-adapter"(id:inputChannel+"-OutboundFileAdapter-id", "auto-create-directory":"true", channel:inputChannel+"EmailTransformer", directory: mailReceiverAdapter.directoryToCopyAttachmentFiles){				
			}
			
			"int-file:inbound-channel-adapter"(id:inputChannel+"-InboundFileAdapter-id", directory: mailReceiverAdapter.directoryToCopyAttachmentFiles, channel:inputChannel+"AttachmentFileAdapter", "prevent-duplicates":"true"){
				"int:poller"(id:"poller-"+inputChannel+"-InboundFileAdapter-id", "fixed-delay":"1000"){
				}
			}
			
			"int-file:file-to-string-transformer"("input-channel":inputChannel+"AttachmentFileAdapter", "output-channel":inputChannel+"AttachmentFileAdapter-Transformer", "delete-files":"true"){
			}
			
			"int:chain"("input-channel":inputChannel+"AttachmentFileAdapter-Transformer", "output-channel":outputChannel){
				
				"int:json-to-object-transformer"(type:typeForConversion){  }
				
				if(instructionNode.instruction.springBean.output!=null && instructionNode.instruction.springBean.output.adapter instanceof MailReceiverAdapter){
					
					"int:header-enricher"(id:inputChannel+"-headerEnricher-id"){
						"int:header"(name:"messageID", expression:"headers['id'].toString()"){ }
					}
			
					"int:transformer"(id:"transformer-"+inputChannel+"-id", ref:inputChannel+"ObjectToApplicationTransformer", "method":"transform"){
						
						if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){
							if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.after){
								"int:request-handler-advice-chain"(){
									"ref"(bean:"eventSourcingAdvice"){  }
								}
							}
						}
	
					}
					 
					 
					 
				} else if(instructionNode.instruction.springBean.input.adapter instanceof MailReceiverAdapter){
					
					"int:header-enricher"(id:inputChannel+"-headerEnricher-id"){
						"int:header"(name:"messageID", expression:"headers['id'].toString()"){ }
					}
					
					if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){
						if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.after){
							"int:transformer"(expression:"payload"){
								"int:request-handler-advice-chain"(){
									"ref"(bean:"eventSourcingAdvice"){ }
								}
							}
						}
					}
			
				}

			}
			
			"bean"(id:inputChannel+"ObjectToApplicationTransformer", class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer"){
				"property"(name:"application", ref:instructionNode.instruction.springBean.name){ }
			}
			
/*			"int:json-to-object-transformer"("input-channel":inputChannel+"AttachmentFileAdapter-Transformer", "output-channel":inputChannel+"AttachmentFileAdapter-JsonTransformer", type:typeForConversion){  }
						
			if(instructionNode.instruction.springBean.output!=null && instructionNode.instruction.springBean.output.adapter instanceof MailReceiverAdapter){
				
				"int:header-enricher"(id:inputChannel+"-headerEnricher-id", "input-channel":inputChannel+"AttachmentFileAdapter-JsonTransformer", "output-channel":inputChannel+"HeaderEnricher"){
					"int:header"(name:"messageID", expression:"headers['id'].toString()"){ }
				}
	
				"int:transformer"(id:"transformer-"+inputChannel+"-id", "input-channel":inputChannel+"HeaderEnricher", "output-channel":outputChannel, ref:inputChannel+"ObjectToApplicationTransformer", "method":"transform"){ }
				 
				 "bean"(id:inputChannel+"ObjectToApplicationTransformer", class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer"){
					 "property"(name:"application", ref:instructionNode.instruction.springBean.name){ }
				 }
				 
			} else if(instructionNode.instruction.springBean.input.adapter instanceof MailReceiverAdapter){
				
				"int:header-enricher"(id:inputChannel+"-headerEnricher-id", "input-channel":inputChannel+"AttachmentFileAdapter-JsonTransformer", "output-channel":outputChannel){
					"int:header"(name:"messageID", expression:"headers['id'].toString()"){ }
				}

			}
*/			
			
			
					
		}
	}

	//def aggregator(InstructionNode instructionNode, List<String> applicationsNamesInExpression){
	def aggregator(InstructionNode instructionNode, String releaseExpression, String transformerExpression, boolean isMultipleArgumentsInExpression){
		
		Instruction instruction = instructionNode.instruction
		 		
		def outputChannel = instructionNode.inputName + "AggregatorOutput"
		
		//def releaseExpression = this.releaseExpression(instructionNode.instruction.variable, applicationsNamesInExpression)
		
		//def size = "size()=="  + applicationsNamesInExpression.size()
		
		//releaseExpression = size + " and " + "(" + releaseExpression + ")"
				
		//String transformerExpression = this.aggregatorTransformerExpression(instructionNode, applicationsNamesInExpression)
		
		//boolean isMultipleArgumentsInExpression = this.multipleArgumentsInExpression(instructionNode, applicationsNamesInExpression)
		
		//println instructionNode
		
		if(instructionNode.options == null){
			instructionNode.options = new QualityOfServicesOptions(sameEvent: false)
		}
		
		boolean sameEvent = instructionNode.options.sameEvent
		
		def clos = {
		
			if(sameEvent == true){
				"int:aggregator"(id:"aggregator-"+instructionNode.inputName+"-id", "input-channel":instructionNode.inputName, "output-channel": instructionNode.inputName + "Transformer", "correlation-strategy-expression":"headers['messageID']", "release-strategy-expression":releaseExpression){
				}
			} else {
				"int:aggregator"(id:"aggregator-"+instructionNode.inputName+"-id", "input-channel":instructionNode.inputName, "output-channel": instructionNode.inputName + "Transformer", "correlation-strategy-expression":"0", "release-strategy-expression":releaseExpression){
				}
			}
					
			"int:transformer"(id:"transformer-"+instructionNode.inputName+"-id", "input-channel":instructionNode.inputName + "Transformer", "output-channel":outputChannel, expression:transformerExpression){	}
			
			if(isMultipleArgumentsInExpression == true){				
				def transformerBean = instructionNode.outputName + "ApplicationsToObjectsTransformerBean"
				"int:transformer"(id:"transformer-"+outputChannel+"-id", "input-channel":outputChannel, "output-channel":instructionNode.outputName, ref:transformerBean, "method":"transform"){ }
				"bean"(id:transformerBean, class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ApplicationsListToObjectsListTransformer"){
				}
			} else {
				def transformerBean = instructionNode.outputName + "ApplicationToObjectTransformerBean"			
				"int:transformer"(id:"transformer-"+outputChannel+"-id", "input-channel":outputChannel, "output-channel":instructionNode.outputName, ref:transformerBean, "method":"transform"){ }
				"bean"(id:transformerBean, class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ApplicationToObjectTransformer"){
				}
			}
			
			if(instructionNode.options!=null && instructionNode.options.queue!=null){
				long queueCapacity = instructionNode.options.queue.capacity
				"int:channel"(id:instructionNode.outputName){
					"int:queue"(capacity: queueCapacity){ }
				}
			}
		}
	}
	
	def failTransformer(InstructionNode instructionNode, String failedServiceName, String failChannel, String errorExpression){
		
		Instruction instruction = instructionNode.instruction
				 
		def outputChannel = instructionNode.inputName + "AggregatorOutput"
		
		def clos = {
		
			"int:channel"(id:failChannel+"-id"){ }
			
			"int:transformer"("input-channel":failChannel+"-id", "output-channel":failChannel+"-errorUnwrapper-output", expression:"@errorUnwrapper.transform(payload)"){
			}
			
			"int:transformer"(id:"transformer-"+failChannel+"-output-id", "input-channel":failChannel+"-errorUnwrapper-output", "output-channel":failChannel+"-ErrorToApplication-output", ref:failChannel+"-TransformerBean", method:"transform"){				
			}

			"bean"(id:failChannel+"-TransformerBean", class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ErrorToApplicationTransformer"){
				"property"(name:"application", ref:failedServiceName){
				}
			}
			
			"int:transformer"("input-channel":failChannel+"-ErrorToApplication-output", "output-channel":failChannel+"-output", expression:errorExpression){				
			}
			
			  
		}
	}
	
	/*private def releaseExpression(String expression, List<String> applicationsNamesInExpression){
		int i=0
		def releaseExpression = ""
		Iterator names = applicationsNamesInExpression.iterator()
		def name
		int index
		while(names.hasNext()){
			name = names.next()
			index = expression.indexOf(name) + name.length() + 1
			expression = expression.substring(index)
			expression = expression.trim()
			if(expression.startsWith("terminates")){
				releaseExpression = releaseExpression + " ([" + i + "].payload instanceof T(orcha.lang.configuration.Application) AND [" + i + "].payload.state==T(orcha.lang.configuration.State).TERMINATED) "
				i++
				index = expression.indexOf("terminates") + "terminates".length() + 1
				if(index >= expression.length()){
					return releaseExpression
				}
				expression = expression.substring(index)
				expression = expression.trim()
				if(expression.startsWith("and")){
					releaseExpression = releaseExpression + " and "
					index = expression.indexOf("and") + "and".length() + 1
					expression = expression.substring(index)
					expression = expression.trim()
				} else if(expression.startsWith("or")){
					releaseExpression = releaseExpression + " or "
					index = expression.indexOf("or") + "or".length() + 1
					expression = expression.substring(index)
					expression = expression.trim()
				}
				
			}
			
		}
		return releaseExpression
	}
	*/
	/*def aggregatorTransformerExpression(InstructionNode instructionNode, List<String> applicationsNamesInExpression){
		
		String transformerExpression = "payload.?["
		
		String nextInstruction
			
		if(instructionNode.next.instruction.instruction == "send"){
		
			nextInstruction = instructionNode.next.instruction.variable
			
			if(applicationsNamesInExpression.contains(nextInstruction)){
				transformerExpression = transformerExpression + "name=='" + nextInstruction + "']"
			}
			
		} else if(instructionNode.next.instruction.instruction == "compute"){
		
			int manyWith = 1;
	
			def withs = instructionNode.next.instruction.withs
			withs.each{ with ->
				
				if(applicationsNamesInExpression.contains(with.with)){
					
					if(manyWith > 1){
						transformerExpression = transformerExpression + " or "
					}
					
					transformerExpression = transformerExpression + "name=='" + with.with + "'"
					
					manyWith++
				}
					
			}
				
			transformerExpression = transformerExpression + "]"
				
			
			return transformerExpression
		}
	}*/

	/*boolean multipleArgumentsInExpression(InstructionNode instructionNode, List<String> applicationsNamesInExpression){
		
		int manyWith = 0
		
		String nextInstruction
			
		if(instructionNode.next.instruction.instruction == "send"){
		
			nextInstruction = instructionNode.next.instruction.variable
			manyWith = 1
			
		} else if(instructionNode.next.instruction.instruction == "compute"){
		
			def withs = instructionNode.next.instruction.withs
			withs.each{ with ->
				
				if(applicationsNamesInExpression.contains(with.with)){			
					manyWith++
				}
					
			}
		}
		
		return manyWith > 1	
	}*/
	
	def eventSourcing(List<InstructionNode> eventsSourcing){
				
		/*String pointCutExpression = "execution(* orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer.transform(*))"
		
		String filteringExpression = "{'"
		
		int index = 0
		
		eventsSourcing.each { instructionNode ->
			if(index > 0){
				filteringExpression = filteringExpression + ",'"
			}
			filteringExpression = filteringExpression + instructionNode.instruction.springBean.name + "'"
			index++
		}
		
		filteringExpression = filteringExpression + "}.contains(payload.name)"*/

		def clos = {
			/*"aop:config"(){
				"aop:aspect"(id:"eventSourcingAdviceAspect", ref:"eventSourcingAdviceBean"){
					"aop:pointcut"(id:"eventSourcingPointcut", expression:pointCutExpression) { }
					"aop:around"("pointcut-ref":"eventSourcingPointcut", method:"logEvent"){ }
				}
			} 
			
			"bean"(name:"eventSourcingAdviceBean", class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.EventSourcingAdvice"){ }
		   */
/*			"int:gateway"(id:"eventSourcingGateway", "default-request-channel":"eventSourcingFilteringAndHeaderEnricherChannel", "service-interface":"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.EventSourcingGateway"){ }

			"int:chain"(id:"eventSourcingFilteringAndHeaderEnricherChannelD", "input-channel":"eventSourcingFilteringAndHeaderEnricherChannel", "output-channel":"eventSourcingChannel"){
				"int:header-enricher"(){
					"int:header"(name:"timestampSession", expression:"@orchaSession.timestamp"){						
					}
				}
			}
						
			"int:channel"(id:"eventSourcingChannel"){
				"int:queue"(capacity:"1000", "message-store":"mongoDbMessageStore"){ }
			}*/
			
			"bean"(id:"eventSourcingAdvice", class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.EventSourcingAdvice"){ }
			
			"int:channel"(id:"eventSourcingChannel"){ }
		   
			"int:header-enricher"("input-channel":"eventSourcingChannel", "output-channel":"eventSourcingQueueChannel"){
				"int:header"(name:"timestampSession", expression:"@orchaSession.timestamp"){ }
			}
			
			"int:channel"(id:"eventSourcingQueueChannel"){
				"int:queue"("message-store":"mongoDbMessageStore"){ }
			}
			  
			"bean"(id:"mongoDbMessageStore", class:"org.springframework.integration.mongodb.store.MongoDbMessageStore"){
				"constructor-arg"(ref:"mongoDbFactory"){ }
			}
			
			"bean"(id:"mongoDbFactory", class:"org.springframework.data.mongodb.core.SimpleMongoDbFactory"){
				"constructor-arg"(){
					"bean"(class:"com.mongodb.Mongo"){ }
				}
				"constructor-arg"(value:"orchaEventSourcing"){ }
			}			
		}
	}
	
	def routerForEventHandler(InstructionNode instructionNode){
		
		Instruction instruction = (Instruction)instructionNode.instruction
		
		def inputChannel = instructionNode.outputName
		
		def clos = {
			
			"int:recipient-list-router"(id:'router-' + inputChannel + '-id', "input-channel":inputChannel){
				
				InstructionNode node = instructionNode.next
				int i=0
				boolean  defaultChannel = true
				
				while(node != null){  
					
					Instruction nextInstruction = node.instruction
					
					String channelName = node.outputName
					
					if(nextInstruction.condition != null){

						String selectorExpression = nextInstruction.condition.replaceFirst(nextInstruction.variable,"payload")
						"int:recipient"(channel:channelName, "selector-expression":selectorExpression){ }
						
					} else {
						"int:recipient"(channel:channelName){ }
						defaultChannel = false
					}
					
					node = node.next
					i++
				}
				
				if(defaultChannel == true){
					"int:recipient"(channel:"loggingChannel"){ }
				}
			}
			
			InstructionNode node = instructionNode.next
			int i=0
				
			while(node != null){
				
				Instruction nextInstruction = node.instruction
				
				String channelName = node.outputName
				
				long queueCapacity = -1
				
				if(node.options!=null && node.options.queue!=null){
					queueCapacity = node.options.queue.capacity
				}
				
				if(queueCapacity != -1){
					
					"int:channel"(id:channelName){
						"int:queue"(capacity: queueCapacity){ }
					}
					
				} else {
				
					"int:channel"(id:channelName){
					}
					
				}
							
				node = node.next
				i++
			}				
		}
	}
	
	def routerForAggregator(InstructionNode instructionNode){
		
		//instructionNode = instructionNode.next
		
		//if(expressionParser.isSeveralWhenWithSameApplicationsInExpression(node)){

			Instruction instruction = (Instruction)instructionNode.instruction
			
			def inputChannel = instructionNode.inputName
			
			def clos = {
				
				"int:recipient-list-router"(id:'router-' + inputChannel + '-id', "input-channel":inputChannel){
					
					InstructionNode node = instructionNode.next
					int i=0
					boolean  defaultChannel = true
					
					while(node != null){
						
						Instruction nextInstruction = node.instruction
						
						String channelName = node.outputName
						
						if(nextInstruction.condition != null){
	
							String selectorExpression = nextInstruction.condition.replaceFirst(nextInstruction.variable,"payload")
							"int:recipient"(channel:channelName, "selector-expression":selectorExpression){ }
							
						} else {
							"int:recipient"(channel:channelName){ }
							defaultChannel = false
						}
						
						node = node.next
						i++
					}
					
					if(defaultChannel == true){
						"int:recipient"(channel:"loggingChannel"){ }
					}
				}
			}
	
		//}
	}
	
	def outputFileAdapter(InstructionNode instructionNode){
		
		def instruction = instructionNode.instruction
		
		def outputName = instruction.variable 
		EventHandler eventHandler = instruction.springBean
		def outputChannel = outputName + "OutputFileChannelAdapter" + eventHandler.name
		String directoryExpression = "@" + eventHandler.name + ".output.adapter.directory"
		String filenameExpression = "@" + eventHandler.name + ".output.adapter.filename"
		
		def clos = {
			
			if(instruction.variableProperty=="result" || instruction.variableProperty=="error"){

				/*if(eventHandler.output.mimeType == "text/plain"){
					"int:object-to-string-transformer"("input-channel":instructionNode.inputName, "output-channel":outputChannel){ }
				} else if(eventHandler.output.mimeType == "application/json"){
					"int:object-to-json-transformer"("input-channel":instructionNode.inputName, "output-channel":outputChannel){ }
				}*/
				
				
				"int:chain"("input-channel":instructionNode.inputName, "output-channel":outputChannel){
	
					if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){
						if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.before){
							"int:transformer"(expression:"payload"){
								"int:request-handler-advice-chain"(){
									"ref"(bean:"eventSourcingAdvice"){ }
								}
							}							
						}
					}
					
					if(eventHandler.output.mimeType == "text/plain"){
						"int:object-to-string-transformer"(){ }
					} else if(eventHandler.output.mimeType == "application/json"){
						"int:object-to-json-transformer"(){ }
					}
				}
				
				
			} else if(instruction.variableProperty == "value"){		// case where the previous instruction is like: receive event from file
																	// and the current instruction is like: send event.value to output
			
				// look for the index i of the current instruction into all the inctructions
				int i=0
				while(i<instructions.size() && instructions.get(i)!=instruction){
					i++
				}
				
				// search the previous instruction (decrease i) so its variable matches
				i--
				while(i>=0 && instruction.variable!=instructions.get(i).variable){
					i--
				}
				
				def instructionToConnectToTheInput = instructions.get(i)
			
				"int:channel"(id:instructionToConnectToTheInput.springIntegrationOutputChannel){ }
				
				"int:chain"("input-channel":instructionToConnectToTheInput.springIntegrationOutputChannel, "output-channel":outputChannel){
					
					if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){
						if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.before){
								"int:transformer"(expression:"payload"){
								"int:request-handler-advice-chain"(){
									"ref"(bean:"eventSourcingAdvice"){ }
								}
							}
						}
					}
									
					if(eventHandler.output.mimeType == "text/plain"){
						"int:object-to-string-transformer"(){ }
					} else if(eventHandler.output.mimeType == "application/json"){
						"int:object-to-json-transformer"(){ }
					}
				}
			
				/*if(eventHandler.output.mimeType == "text/plain"){
					"int:object-to-string-transformer"("input-channel":instructionToConnectToTheInput.springIntegrationOutputChannel, "output-channel":outputChannel){ }
				} else if(eventHandler.output.mimeType == "application/json"){
					"int:object-to-json-transformer"("input-channel":instructionToConnectToTheInput.springIntegrationOutputChannel, "output-channel":outputChannel){ }
				}*/
				
			} else {
				throw new OrchaCompilationException("Property for variable " + instruction.variable + " should be value or result")
			}
			
			
			
			"int:channel"(id:outputChannel){ }
			
			"int-file:outbound-channel-adapter"(id:"file-"+outputName+eventHandler.name+"Channel-id", channel:outputChannel, "directory-expression":directoryExpression, "filename-generator-expression":filenameExpression, "append-new-line":eventHandler.output.adapter.appendNewLine, mode:eventHandler.output.adapter.writingMode.toString(), "auto-create-directory":eventHandler.output.adapter.createDirectory, "delete-source-files":"false"){
			//"int-file:outbound-channel-adapter"(id:"file-"+outputName+eventHandler.name+"Channel-id", channel:outputChannel, directory:eventHandler.output.adapter.directory, "filename-generator-expression":"'"+eventHandler.output.adapter.filename+"'", "append-new-line":eventHandler.output.adapter.appendNewLine, mode:eventHandler.output.adapter.writingMode.toString(), "auto-create-directory":eventHandler.output.adapter.createDirectory, "delete-source-files":"false"){
				
				/*if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){
					if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.before){
						"int:request-handler-advice-chain"(){
							"ref"(bean:"eventSourcingAdvice"){ }
						}
					}
				}*/
			}
			
		}
	}
	
}
