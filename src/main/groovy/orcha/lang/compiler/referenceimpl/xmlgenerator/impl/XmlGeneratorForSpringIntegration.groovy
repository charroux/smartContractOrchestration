
 package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import java.lang.annotation.IncompleteAnnotationException
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoDatabase

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j;
import groovy.xml.QName
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.qualityOfService.CircuitBreakerOption;
import orcha.lang.compiler.qualityOfService.EventSourcingOption;
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServicesOptions;
import orcha.lang.compiler.qualityOfService.QueueOption;
import orcha.lang.compiler.qualityOfService.RetryOption;
import orcha.lang.compiler.referenceimpl.ExpressionParser;
import orcha.lang.compiler.referenceimpl.xmlgenerator.XmlGenerator
import orcha.lang.compiler.referenceimpl.xmlgenerator.connectors.SpringIntegrationConnectors
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.configuration.AMQP_Adapter
import orcha.lang.configuration.Application
import orcha.lang.configuration.ComposeEventAdapter
import orcha.lang.configuration.DatabaseAdapter
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.HttpAdapter
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.MailReceiverAdapter
import orcha.lang.configuration.MailSenderAdapter
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.ScriptServiceAdapter
/*import orcha.lang.configuration.Retry
import orcha.lang.configuration.Queue
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.EventSourcing*/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType
import org.springframework.integration.file.FileHeaders

@Slf4j
class XmlGeneratorForSpringIntegration implements XmlGenerator{
	
	@Autowired
	SpringIntegrationConnectors connectors
	
	@Autowired
	ExpressionParser expressionParser
	
	@Autowired
	QualityOfService qualityOfService
	
	@Autowired
	ApplicationContext context
	
	@Override
	void generate(OrchaCodeParser orchaCodeParser, File xmlSpringContextFile, File xmlQoSSpringContextFile){
		
		this.propagateReceiveEvent(orchaCodeParser)
				
		// Connect together mainly channels of Spring integration
		this.generateInputOutputChannelNames(orchaCodeParser)
		
		qualityOfService.setQualityOfServiceToInstructions(orchaCodeParser)
		
		//this.resumeAtStoppingPoint(orchaCodeParser)
		
		BufferedWriter bufferedWriterSpringContext = new BufferedWriter(new FileWriter(xmlSpringContextFile))
		bufferedWriterSpringContext.writeLine('<beans  xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:stream="http://www.springframework.org/schema/integration/stream" xmlns:int="http://www.springframework.org/schema/integration" xmlns:int-mail="http://www.springframework.org/schema/integration/mail" xmlns:util="http://www.springframework.org/schema/util" xmlns:int-file="http://www.springframework.org/schema/integration/file" xmlns:int-stream="http://www.springframework.org/schema/integration/stream" xmlns:int-http="http://www.springframework.org/schema/integration/http" xmlns:int-groovy="http://www.springframework.org/schema/integration/groovy" xmlns:context="http://www.springframework.org/schema/context" xmlns:task="http://www.springframework.org/schema/task" xmlns:jdbc="http://www.springframework.org/schema/jdbc" xmlns:int-jdbc="http://www.springframework.org/schema/integration/jdbc" xmlns:int-script="http://www.springframework.org/schema/integration/scripting" xmlns:int-event="http://www.springframework.org/schema/integration/event" xmlns:int-amqp="http://www.springframework.org/schema/integration/amqp" xmlns:rabbit="http://www.springframework.org/schema/rabbit" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task.xsd http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration-4.3.xsd http://www.springframework.org/schema/integration/mail http://www.springframework.org/schema/integration/mail/spring-integration-mail-4.3.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd http://www.springframework.org/schema/integration/file http://www.springframework.org/schema/integration/file/spring-integration-file-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd http://www.springframework.org/schema/integration/jdbc http://www.springframework.org/schema/integration/jdbc/spring-integration-jdbc-4.3.xsd http://www.springframework.org/schema/jdbc http://www.springframework.org/schema/jdbc/spring-jdbc-4.3.xsd http://www.springframework.org/schema/integration/http http://www.springframework.org/schema/integration/http/spring-integration-http-4.3.xsd http://www.springframework.org/schema/integration/groovy http://www.springframework.org/schema/integration/groovy/spring-integration-groovy-4.3.xsd http://www.springframework.org/schema/integration/scripting http://www.springframework.org/schema/integration/scripting/spring-integration-scripting-4.3.xsd http://www.springframework.org/schema/integration/event http://www.springframework.org/schema/integration/event/spring-integration-event-4.3.xsd http://www.springframework.org/schema/rabbit http://www.springframework.org/schema/rabbit/spring-rabbit.xsd http://www.springframework.org/schema/integration/amqp http://www.springframework.org/schema/integration/amqp/spring-integration-amqp-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd">')

		def xmlEvent = generateGeneralContext()
		bufferedWriterSpringContext.writeLine(xmlEvent.toString())

		BufferedWriter bufferedWriterQoSSpringContext = new BufferedWriter(new FileWriter(xmlQoSSpringContextFile))
		bufferedWriterQoSSpringContext.writeLine('<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:stream="http://www.springframework.org/schema/integration/stream" xmlns:aop="http://www.springframework.org/schema/aop" xmlns:int="http://www.springframework.org/schema/integration" xmlns:int-file="http://www.springframework.org/schema/integration/file" xmlns:int-stream="http://www.springframework.org/schema/integration/stream" xmlns:int-http="http://www.springframework.org/schema/integration/http" xmlns:int-groovy="http://www.springframework.org/schema/integration/groovy" xmlns:context="http://www.springframework.org/schema/context" xmlns:task="http://www.springframework.org/schema/task" xmlns:jdbc="http://www.springframework.org/schema/jdbc" xmlns:int-jdbc="http://www.springframework.org/schema/integration/jdbc" xmlns:int-script="http://www.springframework.org/schema/integration/scripting" xmlns:int-event="http://www.springframework.org/schema/integration/event" xmlns:int-amqp="http://www.springframework.org/schema/integration/amqp" xmlns:rabbit="http://www.springframework.org/schema/rabbit" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task.xsd http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration-4.3.xsd http://www.springframework.org/schema/integration/file http://www.springframework.org/schema/integration/file/spring-integration-file-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd http://www.springframework.org/schema/integration/jdbc http://www.springframework.org/schema/integration/jdbc/spring-integration-jdbc-4.3.xsd http://www.springframework.org/schema/jdbc http://www.springframework.org/schema/jdbc/spring-jdbc-4.3.xsd http://www.springframework.org/schema/integration/http http://www.springframework.org/schema/integration/http/spring-integration-http-4.3.xsd http://www.springframework.org/schema/integration/groovy http://www.springframework.org/schema/integration/groovy/spring-integration-groovy-4.3.xsd http://www.springframework.org/schema/integration/scripting http://www.springframework.org/schema/integration/scripting/spring-integration-scripting-4.3.xsd http://www.springframework.org/schema/integration/event http://www.springframework.org/schema/integration/event/spring-integration-event-4.3.xsd http://www.springframework.org/schema/rabbit http://www.springframework.org/schema/rabbit/spring-rabbit.xsd http://www.springframework.org/schema/integration/amqp http://www.springframework.org/schema/integration/amqp/spring-integration-amqp-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd">')

		//xmlEvent = generateGeneralContextForQoS()
		//bufferedWriterQoSSpringContext.writeLine(xmlEvent.toString())
		
		def alreadyDoneInstructions = []
		
		def eventsSourcing = []
		
		List<InstructionNode> nodes = orchaCodeParser.findAllNodes()
		
		nodes.each {
			
			generateXMLForInstruction(it, orchaCodeParser, alreadyDoneInstructions, bufferedWriterSpringContext)
			
			if(it.options!=null && it.options.eventSourcing!=null){
				eventsSourcing.add(it)
			}
		}
		
		if(eventsSourcing.size() > 0){
			generateEventSourcingXML(eventsSourcing, bufferedWriterQoSSpringContext)
		}
		
		bufferedWriterSpringContext.writeLine('</beans>')
		bufferedWriterSpringContext.flush()
		bufferedWriterSpringContext.close()
		
		// usefull to write line by line
		String xmlContext = xmlSpringContextFile.text
		String springContexteAsText = XmlUtil.serialize(xmlContext)
		xmlSpringContextFile.withWriter('utf-8') { writer ->
			writer.writeLine springContexteAsText
			writer.close()
		}
		
		log.info 'Transpilatation complete successfully. Orcha orchestrator generated into ' + xmlSpringContextFile.getAbsolutePath()
		
		bufferedWriterQoSSpringContext.writeLine('</beans>')
		bufferedWriterQoSSpringContext.flush()
		bufferedWriterQoSSpringContext.close()
		
		// usefull to write line by line
		xmlContext = xmlQoSSpringContextFile.text
		springContexteAsText = XmlUtil.serialize(xmlContext)
		xmlQoSSpringContextFile.withWriter('utf-8') { writer ->
			writer.writeLine springContexteAsText
			writer.close()
		}
		
		log.info 'Transpilatation complete successfully. QoS for Orcha orchestrator generated into ' + xmlQoSSpringContextFile.getAbsolutePath()
	}
	
	public void propagateSameEvent(InstructionNode nextNode, InstructionNode node){
		if(nextNode.options == null){
			nextNode.options = new QualityOfServicesOptions()
		}
		if(node.options == null){
			nextNode.options.sameEvent = nextNode.instruction.variable
		} else {
			nextNode.options.sameEvent = node.options.sameEvent
		}
	}
	
	
	/**
	 * options is used to indicate if the same event is used:
	 * 
	 * receive event from inputFile
	 * compute code1 with event.value
	 * receive event from inputFile
	 * compute code2 with event.value
	 * 
	 * if yes: the message id will be used as correlation-strategy-expression in the Spring integration aggregator
	 * since the same event generate the same message
	 * 
	 * receive event1 from inputFile condition "event1 == 0"	  		
	 * compute code1 with event1.value
	 * receive event2 from inputFile condition "event2 == 1"
	 * compute code2 with event2.value
	 * 
	 * if not:  the correlation-strategy-expression will be set to 0
	 * 
	 * @param graphOfInstructions
	 * @return
	 */
	private propagateReceiveEvent(OrchaCodeParser orchaCodeParser){
		
		def receiveInstructions = orchaCodeParser.findAllReceiveNodes()
		def nodesAlreadyDone = []
			
		receiveInstructions.each{ receiveNode ->
			orchaCodeParser.depthTraversal(this, "propagateSameEvent", receiveNode, orchaCodeParser, nodesAlreadyDone)	
		}
		
		// are all instructions before a when come from the same receive event ?
		
		// receive EVENT
		// compute appli1 with EVENT.value
		// receive EVENT
		// compute apppl2 with EVENT.value
		// when "appli2 terminates and appl1 terminates"
		
		List<InstructionNode> whenInstructions = orchaCodeParser.findAllWhenNodesWithDifferentApplicationsInExpression()
		//List<InstructionNode> whenInstructions = orchaCodeParser.findAllWhenNodesWithManyApplicationsInExpression()
		
		whenInstructions.each{	whenNode ->
			
			// [compute appli1, compute appli2]
			def beforeWhen = orchaCodeParser.findAllPrecedingNodes(whenNode)
			
			// 2
			int numberOfBeforeWhen = beforeWhen.size()
			
			if(numberOfBeforeWhen != 0){

				if(beforeWhen.getAt(0).options != null){
					
					// EVENT
					String eventName = beforeWhen.getAt(0).options.sameEvent
					
					// 2
					int numberOfSameEvent = beforeWhen.findAll { instrucNode ->
					instrucNode.options.sameEvent == eventName }.size()
							
					if(numberOfSameEvent != numberOfBeforeWhen){
						whenNode.options = new QualityOfServicesOptions(sameEvent: false)
					} else {
						whenNode.options = new QualityOfServicesOptions(sameEvent: true)
					}
				}
				
			}
			
		
		}
	}
/*	*//**
	 * Queue are added to a node from its preceding node because of the receive 
	 * instructions already having in their routers channels in which queues are added.
	 *  
	 * @param graphOfInstructions
	 * @return
	 *//*
	private void qualityOfServices(OrchaCodeParser orchaCodeParser){
		
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
				
			}  else if(it.instruction.instruction == "when"){
		
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
			}
			
			nodeIndex++
		}
	}*/
	
	private void resumeAtStoppingPoint(OrchaCodeParser orchaCodeParser){

		// did resume at stopping point option has been chosen ? 
		List<InstructionNode> computeInstructions = orchaCodeParser.findAllComputeNodes()
		
		List<InstructionNode> computeInstructionsWithEventSourcing = computeInstructions.findAll { it.options!=null && it.options.eventSourcing!=null && it.options.eventSourcing.resumeAtStoppingPoint==true }
		
		def applicationsWithEventSourcing = []
		
		computeInstructionsWithEventSourcing.each {
			applicationsWithEventSourcing.add(it.instruction.springBean)
		}
		
		// look at the events of the last session in the event sourcing message store
		def jsonSlurper = new JsonSlurper()
		
		MongoClient mongoClient = new MongoClient();
	
		MongoDatabase database = mongoClient.getDatabase("orchaEventSourcing")
		MongoCollection collection = database.getCollection("messages")
		// sort: the more recent session first
		FindIterable iterable = collection.find(com.mongodb.client.model.Filters.exists("headers.timestampSession", true)).sort(new BasicDBObject("headers.timestampSession", -1))
		
		// get all the events from the last session
		def lastSession = []
		MongoCursor cursor = iterable.iterator()
		Object message
		Application application
		def jsonApplication
		
		if(cursor.hasNext()){
			message = cursor.next()
			jsonApplication = jsonSlurper.parseText(message.payload.toJson())
			jsonApplication.remove("_class")
			application = new Application(jsonApplication)
			lastSession.add(application)
		}
		def jsonMessage = jsonSlurper.parseText(message.toJson())
		
		Object nextMessage
		while(cursor.hasNext()){
			nextMessage = cursor.next()
			def nextJsonMessage = jsonSlurper.parseText(nextMessage.toJson())
			if(jsonMessage.headers.timestampSession.$numberLong == nextJsonMessage.headers.timestampSession.$numberLong){
				jsonApplication = jsonSlurper.parseText(nextMessage.payload.toJson())
				jsonApplication.remove("_class")
				application = new Application(jsonApplication)
				lastSession.add(application)			
			}
		}
		
		// If the last session has completed => nothing to resume => just returns
				
		def applicationsNameInSession = []
		lastSession.each {
			applicationsNameInSession.add(it.name)
		}
		
		def applicationsNameInWithEventSourcing = []
		applicationsWithEventSourcing.each {
			applicationsNameInWithEventSourcing.add(it.name)
		}
		
		if(applicationsNameInSession == applicationsNameInWithEventSourcing){
			return
		} 

		// for all found events :
		//	retrieve the related compute instruction
		// 	disable the auto-startup for all previous instructions of the compute instruction
		
		List<InstructionNode> computeAlreadyDoneInstructions = computeInstructions.findAll { applicationsNameInSession.contains(it.instruction.springBean.name) }
		
		List<InstructionNode> precedingNodes
		
		computeAlreadyDoneInstructions.each { instruction ->
			precedingNodes = orchaCodeParser.findAllPrecedingNodes(instruction)
			
			/*precedingNodes.each { precedingInstruction ->
				precedingInstruction.options.autoStartup = false				
			}*/
		}
		  
	}
	
	/*private QueueOption getQueueOption(InstructionNode instructionNode) throws OrchaConfigurationException{
		
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
			
			int intervalMultiplierBetwennAttemps = beanClass.getAnnotation(Retry.class).intervalMultiplierBetwennAttemps()
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
			
			return new RetryOption(maxNumberOfAttempts: maxNumberOfAttempts, intervalBetweenTheFirstAndSecondAttempt: intervalBetweenTheFirstAndSecondAttempt, intervalMultiplierBetwennAttemps: intervalMultiplierBetwennAttemps, maximumIntervalBetweenAttempts: maximumIntervalBetweenAttempts, orderInChain: orderInChain )
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
			String className = "orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer"
			boolean resumeAtStoppingPoint = beanClass.getAnnotation(EventSourcing.class).resumeAtStoppingPoint()
			String className = instructionNode.instruction.springBean.output.adapter.javaClass
			String end = className.substring(1)
			String begin = className.substring(0, 1)
			className = begin.toLowerCase() + end
			Object object = context.getBean(className)
			className = object.getClass().getCanonicalName()	
				
			return new EventSourcingOption(eventName: eventName, className: className, resumeAtStoppingPoint: resumeAtStoppingPoint)
			
		} else {
			return null
		}
	}*/
	
	/**
	 * Connect together mainly channels of Spring integration
	 * @param orchaCodeParser
	 */
	private void generateInputOutputChannelNames(OrchaCodeParser orchaCodeParser){
		
		List<InstructionNode> nodes = orchaCodeParser.findAllReceiveNodes()
		
		nodes.each { node ->
			
			node.inputName = node.instruction.springBean.name + '-InputChannel'
			node.outputName = node.instruction.springBean.name + "-OutputChannel"
			
		}
		
		List<InstructionNode> graphOfInstructions = orchaCodeParser.findAllNodes()
		
		graphOfInstructions.each{ node ->
			InstructionNode nextNode = orchaCodeParser.findNextRawNode(node)
			if(nextNode != null){
				nextNode.inputName = node.outputName
			}			
		}
		
		nodes = orchaCodeParser.findAllReceiveNodesWithTheSameEvent()
		
		InstructionNode rootNode
		
		nodes.each { node ->
			
			node.inputName = node.instruction.springBean.name + '-InputChannel'
			node.outputName = node.instruction.springBean.name + "-OutputChannel"
			
			rootNode = node
			int index = 1
			def alreadyDoneInstructions = []
			
			while(node.next != null){
						
				node.next.inputName = rootNode.outputName
				node.next.outputName = rootNode.outputName + "Route" + index
							
				InstructionNode receiveNode = orchaCodeParser.findNextRawNode(node, alreadyDoneInstructions)
				alreadyDoneInstructions.add(receiveNode)
				
				InstructionNode nextToReceiveNode = orchaCodeParser.findNextRawNode(receiveNode)
				nextToReceiveNode.inputName = node.next.outputName
				
				node = node.next
				index++
			}
		}
		
		nodes = orchaCodeParser.findAllComputeNodes()
		nodes.each { node ->
			node.outputName = node.instruction.springBean.name + "Output"
		}
		
		nodes = orchaCodeParser.findAllWhenNodesWithDifferentApplicationsInExpression()
		
		nodes.each { node ->
			
			def orchaExpression = node.instruction.variable
			
			List<String> applicationsNames = expressionParser.getApplicationsNamesInExpression(orchaExpression, graphOfInstructions)
									
			String aggregatorName = ""
			for(String name: applicationsNames){
				aggregatorName = aggregatorName + name
			}
									
			node.inputName = aggregatorName + "AggregatorInput"
			node.outputName = aggregatorName + "AggregatorOutputTransformer"
				
			InstructionNode nextToWhenNode = orchaCodeParser.findNextRawNode(node)
			nextToWhenNode.inputName = node.outputName
		}
		
		// when  "selectBestVendor terminates condition price>1000"
		// send selectBestVendor.result to outputFile1
		// when  "selectBestVendor terminates condition price<=1000"
		// send selectBestVendor.result to outputFile2
		
		nodes = orchaCodeParser.findAllWhenNodesWithTheSameApplicationsInExpression()
		
		nodes.each { node ->
			
			rootNode = node
			
			node = node.next	// the first node is an empty node with only adjacent nodes
			
			String orchaExpressionForNode = node.instruction.variable
			
			List<String> applicationsNames = expressionParser.getApplicationsNamesInExpression(orchaExpressionForNode, graphOfInstructions)
	
			String aggregatorName = ""
			for(String name: applicationsNames){
				aggregatorName = aggregatorName + name
			}
			
			rootNode.inputName = aggregatorName + "AggregatorInput"
			rootNode.outputName = aggregatorName + "AggregatorOutputTransformer"
	
			int index = 1
			
			while(node != null){
					
				orchaExpressionForNode = node.instruction.variable
					
				if(expressionParser.isFailExpression(node, orchaCodeParser.findAllNodes()) == true){
					
					String failChannel = expressionParser.failChannel(node, graphOfInstructions)
					failChannel = failChannel  + "-output"
					
					List<InstructionNode> nextNodes = orchaCodeParser.findNextNode(node)
					nextNodes.each { nextNode ->
						nextNode.inputName = failChannel
					}
					
				} else if(expressionParser.isSeveralWhenWithSameApplicationsInExpression(node)){
				
					node.inputName = rootNode.inputName
					node.outputName = rootNode.inputName + "Route" + index
	
					InstructionNode whenNode = orchaCodeParser.findAdjacentNode(node)
				
					whenNode.inputName = node.outputName
					whenNode.outputName = rootNode.outputName + "Route" + index
					
					InstructionNode nextTowhenNode = orchaCodeParser.findNextRawNode(whenNode)
					nextTowhenNode.inputName = whenNode.outputName
					
					index++

				} else {
				
					node.inputName = aggregatorName + "AggregatorInput"
					node.outputName = aggregatorName + "AggregatorOutputTransformer"
				
					List<InstructionNode> nextNodes = orchaCodeParser.findNextNode(node)
					InstructionNode nextNode = nextNodes.getAt(0)
					nextNode.inputName = node.outputName
				}
				
				node = node.next
				
			}
		}
		
		InstructionNode node
		
		nodes = orchaCodeParser.findAllNodes()
		for(int i=nodes.size()-1; i>=0; i--){
			node = nodes.getAt(i)
			if(node.instruction.instruction == "when"){
				List<InstructionNode> previousNodes = orchaCodeParser.findAllRawPrecedingNodes(node)
				previousNodes.each { beforeWhenNode ->
					if(beforeWhenNode.instruction.instruction != "when"){
						// isn't a when node with a fails :
						// compute appli1
						// when "appli1 fails"
						String orchaExpression = node.instruction.variable
						if(orchaExpression == null){
							orchaExpression = node.next.instruction.variable
						}
						if(expressionParser.isComputeFailsInExpression(beforeWhenNode, orchaExpression) == false){
							beforeWhenNode.outputName = node.inputName
						}						
					}
				}
			}
		}
		
	}
		
	private generateEventSourcingXML(def eventsSourcing, BufferedWriter bufferedWriter){
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true
		bufferedWriter.writeLine(xml.bind(connectors.eventSourcing(eventsSourcing)).toString())
	}
	
	private void generateXMLForInstruction(InstructionNode instructionNode, OrchaCodeParser orchaCodeParser, List<Instruction>alreadyDoneInstructions, BufferedWriter bufferedWriter){
		
		List<Instruction> graphOfInstructions = orchaCodeParser.findAllNodes()
		
		Instruction instruction = instructionNode.instruction
			
		if(instruction.instruction == "receive"){
			
			if( alreadyDoneInstructions.contains(instruction) == false){

				def xmlEvent = generateReceiveEventHandler(instructionNode)
				bufferedWriter.writeLine(xmlEvent.toString())
				
				if(instructionNode.next.instruction.instruction == "receive"){
					
					xmlEvent = generateRouterForEventHandlers(instructionNode)
					bufferedWriter.writeLine(xmlEvent.toString())
					
					InstructionNode node = instructionNode.next
					
					while(node != null){
						alreadyDoneInstructions.add(node.instruction)
						node = node.next
					}
				} 

			}
			
		} else if(instruction.instruction == "compute"){
		
			String failChannel = expressionParser.failChannel(instructionNode, orchaCodeParser)
			
			// when nodes
			List<InstructionNode> nodes = orchaCodeParser.findNextNode(instructionNode)
			
			// is a when node with a fails :
			// compute appli1
			// when "appli1 fails"
			boolean computeFails = (null != nodes.find { expressionParser.isComputeFailsInExpression(instructionNode, it.instruction.variable) })
					
			generateApplication(instructionNode, computeFails, failChannel, bufferedWriter)
			
		} else if(instruction.instruction == "when"){
		
			if(instructionNode.next.instruction.instruction == "when"){
				
				if(expressionParser.isSeveralWhenWithSameApplicationsInExpression(instructionNode.next)){
					def xmlEvent = generateRouterForAggregator(instructionNode)
					bufferedWriter.writeLine(xmlEvent.toString())
				}
								
			} else {
			
				String orchaExpression = instruction.variable
			
				List<InstructionNode> precedingNodes = orchaCodeParser.findAllPrecedingNodes(instructionNode)
				
				if(precedingNodes.size()>0 && expressionParser.isComputeFailsInExpression(precedingNodes.getAt(0), orchaExpression)==false){

					String releaseExpression = expressionParser.releaseExpression(orchaExpression, graphOfInstructions)
					String transformerExpression = expressionParser.aggregatorTransformerExpression(orchaExpression, instructionNode, graphOfInstructions)
					boolean isMultipleArgumentsInExpression = expressionParser.isMultipleArgumentsInExpression(orchaExpression, instructionNode, graphOfInstructions)
					
					def xmlEvent = generateAggregator(instructionNode, releaseExpression, transformerExpression, isMultipleArgumentsInExpression)
					
					bufferedWriter.writeLine(xmlEvent.toString())
	
				} else {
				
					
					List<InstructionNode> previousNodes = orchaCodeParser.findAllRawPrecedingNodes(instructionNode)
					
					previousNodes.each { previousNode ->
						
						boolean computeFails = expressionParser.isComputeFailsInExpression(previousNode, instruction.variable)
						if(computeFails == true){
							List<InstructionNode> nextNodes = orchaCodeParser.findNextNode(instructionNode)
							if(nextNodes.size() > 0){
								InstructionNode nextNode = nextNodes.getAt(0)
								def errorExpression = 'payload.error'
								if(nextNode.instruction.withs.size() > 0){
									String withProperty = nextNode.instruction.withs[0].withProperty
									if(withProperty != "result"){
										errorExpression = errorExpression + '.' + withProperty
									}
								}
												
								String failedServiceName = expressionParser.failedServiceName(instructionNode, graphOfInstructions)	
								String failChannel = expressionParser.failChannel(instructionNode, graphOfInstructions)
								
								def xmlEvent = generateFailTransformer(instructionNode, failedServiceName, failChannel, errorExpression)
								
								bufferedWriter.writeLine(xmlEvent.toString())
							}
						}
					}
				}			
			}
		
		} else if(instruction.instruction == "send"){
		
			def xmlEvent = generateSendEventHandler(instructionNode)
			bufferedWriter.writeLine(xmlEvent.toString())
		
		}

	}
	
	private void generateApplication(InstructionNode instructionNode, boolean computeFails, String failChannel, BufferedWriter bufferedWriter){
		
		if(instructionNode.instruction.springBean.input.adapter instanceof JavaServiceAdapter){
			
			def xml = new StreamingMarkupBuilder()
			xml.useDoubleQuotes = true
			boolean isScript = false
			def xmlEvent = xml.bind(connectors.serviceActivator(instructionNode, computeFails, failChannel, isScript))
			bufferedWriter.writeLine(xmlEvent.toString())
			
		} else if(instructionNode.instruction.springBean.input.adapter instanceof ScriptServiceAdapter){
		
			def xml = new StreamingMarkupBuilder()
			xml.useDoubleQuotes = true
			boolean isScript = true
			def xmlEvent = xml.bind(connectors.serviceActivator(instructionNode, computeFails, failChannel, isScript))
			bufferedWriter.writeLine(xmlEvent.toString())
			
		}
		
		/*if(instructionNode.instruction.springBean.input.adapter instanceof MailReceiverAdapter){
				
			def xml = new StreamingMarkupBuilder()
			xml.useDoubleQuotes = true
			def xmlEvent = xml.bind(connectors.mailReceiverAdapter(instructionNode))
			bufferedWriter.writeLine(xmlEvent.toString())
			
		} */
		
		if(instructionNode.instruction.springBean.input.adapter instanceof MailSenderAdapter){
			
			def xml = new StreamingMarkupBuilder()
			xml.useDoubleQuotes = true
			def xmlEvent = xml.bind(connectors.mailSenderAdapter(instructionNode))
			bufferedWriter.writeLine(xmlEvent.toString())
			
		}
		
	/*	if(instructionNode.instruction.springBean.output.adapter instanceof MailSenderAdapter){
			
			def xml = new StreamingMarkupBuilder()
			xml.useDoubleQuotes = true
			def xmlEvent = xml.bind(connectors.mailSenderAdapter(instructionNode))
			bufferedWriter.writeLine(xmlEvent.toString())
			
		}*/
		
		if(instructionNode.instruction.springBean.output.adapter instanceof MailReceiverAdapter){
			
			def xml = new StreamingMarkupBuilder()
			xml.useDoubleQuotes = true
			def xmlEvent = xml.bind(connectors.mailReceiverAdapter(instructionNode))
			bufferedWriter.writeLine(xmlEvent.toString())
			
		}
		
		/*if((instructionNode.instruction.springBean.input.adapter instanceof JavaServiceAdapter == false) && 
			(instructionNode.instruction.springBean.input.adapter instanceof ScriptServiceAdapter == false) &&
			(instructionNode.instruction.springBean.input.adapter instanceof MailReceiverAdapter == false) &&
			(instructionNode.instruction.springBean.output.adapter instanceof MailSenderAdapter == false)){
				
			def message = instructionNode.instruction.springBean.input.adapter.toString() + ' is not supported yet.'
			throw new OrchaCompilationException(message)
		}*/
		
		
	}
	
	/*def inputHttpAdapterContext = {
		Instruction instruction ->
		def inputName = instruction.springBean.name
		//def outputName = instruction.variable
		def inputChannel = inputName + 'InputChannel'
		def gatewayReplyChannel = inputName + "ReplyChannel"
		def outputChannel = inputName + "OutputChannel"
		instruction.springIntegrationOutputChannel = outputChannel
		instruction.springIntegrationOutputBeanId = "header-enricher-"+inputChannel+"-id"
		def serviceInterface = "myservice." + inputName.substring(0, 1).toUpperCase() + inputName.substring(1) + "Gateway"
		def id = "headers['id'].toString()"
		def clos = {
	
			//"int:gateway"(id:"gateway-"+inputChannel+"-id", "default-request-channel":inputChannel, "default-reply-channel":gatewayReplyChannel, "service-interface":serviceInterface){ }
			
			"int:gateway"(id:"gateway-"+inputChannel+"-id", "default-request-channel":inputChannel, "service-interface":serviceInterface){ }
			
			"int:channel"(id:inputChannel){ }
			
			"int:header-enricher"(id:instruction.springIntegrationOutputBeanId, "input-channel":inputChannel, "output-channel":instruction.springIntegrationOutputChannel){
				"int:header"(name:"messageID", expression:id){ }
			}
				
				   
		}
	}*/
	

/*	def inputComposeEventAdapterContext = {
		Instruction instruction ->
		//def inputName = instruction.springBean.name
		//instruction.springIntegrationOutputChannel = inputName + "OutputChannel"
		instruction.springIntegrationOutputChannel = "composeEventChannel"
		def clos = {
	
			"int-event:inbound-channel-adapter"(channel:"composeEventChannel", "event-types":"org.olabdynamics.compose.event.ComposeEvent"){  }
			
		}
	}*/
	
	private def generateGeneralContext(){
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true
		return xml.bind{
			"int:logging-channel-adapter"(id:"loggingChannel", level:"INFO"){
			}
			"stream:stderr-channel-adapter"(channel:"errorChannel", "append-newline":"true"){
			}	
			"bean"(id:"errorUnwrapper", class:"orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ErrorUnwrapper"){				
			}			
			"int:channel"(id:"recoveryChannel"){ }			
			"int:transformer"("input-channel":"recoveryChannel", "output-channel":"loggingChannel", "expression":"'Failure after many attemps for the message :' + payload.failedMessage.payload"){ }			
		}
	}
	
/*	private def generateGeneralContextForQoS(){
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true
		return xml.bind{			
			"aop:aspectj-autoproxy"(){ }
		}
	}*/
	
	private def generateReceiveEventHandler(InstructionNode instructionNode){
		def EventHandler eventHandler = instructionNode.instruction.springBean
		
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true
		
		if(eventHandler.input!=null && eventHandler.input.adapter instanceof HttpAdapter){
				
			return xml.bind(inputHttpAdapterContext(instructionNode.instruction))
			
		} else if(eventHandler.input!=null && eventHandler.input.adapter instanceof InputFileAdapter){
		
			return xml.bind(connectors.inputFileAdapter(instructionNode))
		
		} else if(eventHandler.input!=null && eventHandler.input.adapter instanceof MailReceiverAdapter){
		
			return xml.bind(connectors.mailReceiverAdapter(instructionNode))
			
		} else if(eventHandler.input!=null && eventHandler.input.adapter instanceof MailSenderAdapter){
		
			return xml.bind(connectors.mailSenderAdapter(instructionNode))
			
		} else if(eventHandler.input!=null && eventHandler.input.adapter instanceof ComposeEventAdapter){
		
			return xml.bind(inputComposeEventAdapterContext(instructionNode.instruction))
			
		} else {
			throw new OrchaCompilationException(eventHandler.toString() + " not supported yet.")
		}
	}

	private def generateSendEventHandler(InstructionNode instructionNode){
		def EventHandler eventHandler = instructionNode.instruction.springBean
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true

		if(eventHandler.output.adapter instanceof DatabaseAdapter){
			
			return xml.bind(outputDatabaseAdapterContext(instruction))
			
		} else if(eventHandler.output.adapter instanceof OutputFileAdapter){
				
			return xml.bind(connectors.outputFileAdapter(instructionNode))
			
		} else if(eventHandler.output.adapter instanceof MailSenderAdapter){
				
			return xml.bind(connectors.mailSenderAdapter(instructionNode))
			
		} else {
			throw new OrchaCompilationException(eventHandler.toString() + " not supported yet.")
		}
	}
	
/*	def outputDatabaseAdapterContext = {
		Instruction instruction ->
		def inputName = instruction.variable
		EventHandler eventHandler = instruction.springBean
		//String inputName, EventHandler eventHandler ->
		//instruction.springIntegrationInputChannel = inputName + 'OutputChannel'
		//def outputChannel = inputName + 'OutputChannel'
		def transformerBean = instruction.springIntegrationInputChannel + "TransformerBean"
		def databaseChannel = inputName + 'DatabaseChannel'
		def dataSourceBeanID = eventHandler.name + "DataSource"
		def dataSource = "org.springframework.jdbc.datasource.DriverManagerDataSource"
		def driver = eventHandler.output.adapter.dataSource.driver
		def url = eventHandler.output.adapter.dataSource.url
		def username = eventHandler.output.adapter.dataSource.username
		def password = eventHandler.output.adapter.dataSource.password
		def request = eventHandler.output.adapter.request
		def persitSpelSource = eventHandler.name + 'PersitSpelSource'
		instruction.springIntegrationInputBeanId = "transformer-"+instruction.springIntegrationInputChannel+"-id"
		def clos = {
	
			"int:transformer"(id:instruction.springIntegrationInputBeanId, "input-channel":instruction.springIntegrationInputChannel, "output-channel":databaseChannel, ref:transformerBean, "method":"transform"){ }
			
			"bean"(id:transformerBean, class:"org.olabdynamics.compose.tools.code.ApplicationToObjectTransformer"){
			}
	
			"bean"(id:dataSourceBeanID, class:dataSource){
				"property"(name:"driverClassName", value:driver){ }
				"property"(name:"url", value:url){ }
				"property"(name:"username", value:username){ }
				"property"(name:"password", value:password){ }
			}
	
			"int:channel"(id:databaseChannel){ }
			
			"int-jdbc:outbound-channel-adapter"(id:"outbound-channel-adapter-"+instruction.springIntegrationInputChannel+"-id", "data-source":dataSourceBeanID, channel:databaseChannel, query:request, "sql-parameter-source-factory":persitSpelSource){ }
			
			int beginIndex = request.indexOf("(")
			int endIndex = request.indexOf(")")
			String dataBaseColumn = request.substring(beginIndex+1, endIndex)
			String[] columns = dataBaseColumn.split(",")
	
			"bean"(id:persitSpelSource, class:"org.springframework.integration.jdbc.ExpressionEvaluatingSqlParameterSourceFactory"){
				"property"(name:"parameterExpressions"){
					"map"(){
						//"entry"(key:"result", value:"payload"){  }
						for(String column: columns){
							column = column.trim()
							if(column == "payload"){
								"entry"(key:column, value:"payload"){  }
							} else {
								"entry"(key:column, value:"payload." + column){  }
							}
							
						}
					}
				}
			}
		}
	}

*/
	

	/*private def generateAggregator(InstructionNode instructionNode, List<String> applicationsNamesInExpression){
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true
		return xml.bind(connectors.aggregator(instructionNode, applicationsNamesInExpression))
	}*/
	
	private def generateRouterForAggregator(InstructionNode instructionNode){
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true
		return xml.bind(connectors.routerForAggregator(instructionNode))
	}
	
	private def generateAggregator(InstructionNode instructionNode, String releaseExpression, String transformerExpression, boolean isMultipleArgumentsInExpression){
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true
		return xml.bind(connectors.aggregator(instructionNode, releaseExpression, transformerExpression, isMultipleArgumentsInExpression))
	}
	
	private def generateFailTransformer(InstructionNode instructionNode, String failedServiceName, String failChannel, String errorExpression){
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true
		return xml.bind(connectors.failTransformer(instructionNode, failedServiceName, failChannel, errorExpression))
	}
	
	private def generateRouterForEventHandlers(InstructionNode instructionNode){
		def xml = new StreamingMarkupBuilder()
		xml.useDoubleQuotes = true
		return xml.bind(connectors.routerForEventHandler(instructionNode))
	}
	
}
