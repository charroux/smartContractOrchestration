package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import java.io.BufferedWriter
import java.io.File
import java.util.List
import java.nio.file.Files

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.input.SAXBuilder
import org.jdom2.output.XMLOutputter
import org.jdom2.output.support.XMLOutputProcessor
import org.springframework.beans.factory.annotation.Autowired

import groovy.util.logging.Slf4j
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServicesOptions
import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.referenceimpl.xmlgenerator.XmlGenerator
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.configuration.ComposeEventAdapter
import orcha.lang.configuration.ConfigurableProperties
import orcha.lang.configuration.DatabaseAdapter
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.HttpAdapter
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.MailReceiverAdapter
import orcha.lang.configuration.MailSenderAdapter
import orcha.lang.configuration.MessagingMiddlewareAdapter
import orcha.lang.configuration.OrchaServiceAdapter
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.contract.ContractGenerator.Format

@Slf4j
class JDom2XmlGeneratorForSpringIntegration implements XmlGenerator{

	@Autowired
	ExpressionParser expressionParser
	
	@Autowired
	QualityOfService qualityOfService
	
	@Override
	public void generate(OrchaCodeVisitor orchaCodeParser, File destinationDirectory) {
		
		String xmlSpringContextFileName = orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"
		String xmlSpringContent = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextFileName
		File xmlSpringContextFile = new File(xmlSpringContent)
		
		String xmlSpringContextQoSFileName = orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
		String xmlQoSSpringContent = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextQoSFileName
		File xmlQoSSpringContextFile = new File(xmlQoSSpringContent)
		
		this.propagateReceiveEvent(orchaCodeParser)
		
		// Connect together mainly channels of Spring integration
		this.generateInputOutputChannelNames(orchaCodeParser)
		
		qualityOfService.setQualityOfServiceToInstructions(orchaCodeParser)
		
		//this.resumeAtStoppingPoint(orchaCodeParser)
		
		BufferedWriter bufferedWriterSpringContext = new BufferedWriter(new FileWriter(xmlSpringContextFile))
		
		bufferedWriterSpringContext.writeLine('<beans xmlns="http://www.springframework.org/schema/beans" xmlns:context="http://www.springframework.org/schema/context" xmlns:int="http://www.springframework.org/schema/integration" xmlns:int-amqp="http://www.springframework.org/schema/integration/amqp" xmlns:int-event="http://www.springframework.org/schema/integration/event" xmlns:int-file="http://www.springframework.org/schema/integration/file" xmlns:int-groovy="http://www.springframework.org/schema/integration/groovy" xmlns:int-http="http://www.springframework.org/schema/integration/http" xmlns:int-jdbc="http://www.springframework.org/schema/integration/jdbc" xmlns:int-mail="http://www.springframework.org/schema/integration/mail" xmlns:int-script="http://www.springframework.org/schema/integration/scripting" xmlns:int-stream="http://www.springframework.org/schema/integration/stream" xmlns:jdbc="http://www.springframework.org/schema/jdbc" xmlns:rabbit="http://www.springframework.org/schema/rabbit" xmlns:task="http://www.springframework.org/schema/task" xmlns:util="http://www.springframework.org/schema/util" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task.xsd http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration-5.0.xsd http://www.springframework.org/schema/integration/mail http://www.springframework.org/schema/integration/mail/spring-integration-mail-5.0.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd http://www.springframework.org/schema/integration/file http://www.springframework.org/schema/integration/file/spring-integration-file-5.0.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-5.0.xsd http://www.springframework.org/schema/integration/jdbc http://www.springframework.org/schema/integration/jdbc/spring-integration-jdbc-5.0.xsd http://www.springframework.org/schema/jdbc http://www.springframework.org/schema/jdbc/spring-jdbc-5.0.xsd http://www.springframework.org/schema/integration/http http://www.springframework.org/schema/integration/http/spring-integration-http-5.0.xsd http://www.springframework.org/schema/integration/groovy http://www.springframework.org/schema/integration/groovy/spring-integration-groovy-5.0.xsd http://www.springframework.org/schema/integration/scripting http://www.springframework.org/schema/integration/scripting/spring-integration-scripting-5.0.xsd http://www.springframework.org/schema/integration/event http://www.springframework.org/schema/integration/event/spring-integration-event-5.0.xsd http://www.springframework.org/schema/rabbit http://www.springframework.org/schema/rabbit/spring-rabbit.xsd http://www.springframework.org/schema/integration/amqp http://www.springframework.org/schema/integration/amqp/spring-integration-amqp-5.0.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-5.0.xsd">')
		bufferedWriterSpringContext.writeLine('</beans>')
		bufferedWriterSpringContext.flush()
		bufferedWriterSpringContext.close()
		
		SAXBuilder builder = new SAXBuilder()
		
		Document xmlSpringIntegration = builder.build(xmlSpringContextFile)
 
		generateGeneralContext(xmlSpringIntegration)
		
		def alreadyDoneInstructions = []
		 
		 def eventsSourcing = []
		 
		 List<InstructionNode> nodes = orchaCodeParser.findAllNodes()
		 
		 nodes.each {
			 
			 generateXMLForInstruction(it, destinationDirectory, orchaCodeParser, alreadyDoneInstructions, xmlSpringIntegration)
			 
			 if(it.options!=null && it.options.eventSourcing!=null){
				 eventsSourcing.add(it.options.eventSourcing)
			 }
		 }		
		
		this.exportToXML(xmlSpringIntegration, xmlSpringContextFile)

		log.info 'Transpilatation complete successfully. Orcha orchestrator generated into ' + xmlSpringContextFile.getAbsolutePath()
		
		BufferedWriter bufferedWriterQoSSpringContext = new BufferedWriter(new FileWriter(xmlQoSSpringContextFile))
		bufferedWriterQoSSpringContext.writeLine('<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:stream="http://www.springframework.org/schema/integration/stream" xmlns:aop="http://www.springframework.org/schema/aop" xmlns:int="http://www.springframework.org/schema/integration" xmlns:int-file="http://www.springframework.org/schema/integration/file" xmlns:int-stream="http://www.springframework.org/schema/integration/stream" xmlns:int-http="http://www.springframework.org/schema/integration/http" xmlns:int-groovy="http://www.springframework.org/schema/integration/groovy" xmlns:context="http://www.springframework.org/schema/context" xmlns:task="http://www.springframework.org/schema/task" xmlns:jdbc="http://www.springframework.org/schema/jdbc" xmlns:int-jdbc="http://www.springframework.org/schema/integration/jdbc" xmlns:int-script="http://www.springframework.org/schema/integration/scripting" xmlns:int-event="http://www.springframework.org/schema/integration/event" xmlns:int-amqp="http://www.springframework.org/schema/integration/amqp" xmlns:rabbit="http://www.springframework.org/schema/rabbit" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task.xsd http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration-4.3.xsd http://www.springframework.org/schema/integration/file http://www.springframework.org/schema/integration/file/spring-integration-file-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd http://www.springframework.org/schema/integration/jdbc http://www.springframework.org/schema/integration/jdbc/spring-integration-jdbc-4.3.xsd http://www.springframework.org/schema/jdbc http://www.springframework.org/schema/jdbc/spring-jdbc-4.3.xsd http://www.springframework.org/schema/integration/http http://www.springframework.org/schema/integration/http/spring-integration-http-4.3.xsd http://www.springframework.org/schema/integration/groovy http://www.springframework.org/schema/integration/groovy/spring-integration-groovy-4.3.xsd http://www.springframework.org/schema/integration/scripting http://www.springframework.org/schema/integration/scripting/spring-integration-scripting-4.3.xsd http://www.springframework.org/schema/integration/event http://www.springframework.org/schema/integration/event/spring-integration-event-4.3.xsd http://www.springframework.org/schema/rabbit http://www.springframework.org/schema/rabbit/spring-rabbit.xsd http://www.springframework.org/schema/integration/amqp http://www.springframework.org/schema/integration/amqp/spring-integration-amqp-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd">')
		bufferedWriterQoSSpringContext.writeLine('</beans>')
		bufferedWriterQoSSpringContext.flush()
		bufferedWriterQoSSpringContext.close()
		
		Document xmlQoSSpringIntegration = builder.build(xmlQoSSpringContextFile)
		
		if(eventsSourcing.size() > 0){
			
			EventSourcing eventSourcing = new EventSourcing(xmlQoSSpringIntegration)
			eventSourcing.eventSourcing(eventsSourcing)
			
		}	
		
		this.exportToXML(xmlQoSSpringIntegration, xmlQoSSpringContextFile)
		
		log.info 'Transpilatation complete successfully. QoS for Orcha orchestrator generated into ' + xmlQoSSpringContextFile.getAbsolutePath()
		
		File oldFile = new File(xmlSpringContent)
		
		// used when the an executable jar is built
		def xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"
		File newFile = new File(xmlSpringContentInSrc);
		FileOutputStream fos = new FileOutputStream(newFile)
		Files.copy(oldFile.toPath(), fos);
		fos.close()

		log.info 'Transpilatation complete successfully. Orcha orchestrator copied into ' + xmlSpringContentInSrc
		
		// Update temporary file
		xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + "main" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"
		newFile = new File(xmlSpringContentInSrc)
		fos = new FileOutputStream(newFile)
		Files.copy(oldFile.toPath(), fos);
		fos.close()
		
		log.info 'Transpilatation complete successfully. Orcha orchestrator copied into ' + xmlSpringContentInSrc
		
		oldFile = new File(xmlQoSSpringContent)
				
		// used when the an executable jar is built
		xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
		newFile = new File(xmlSpringContentInSrc);
		fos = new FileOutputStream(newFile)
		Files.copy(oldFile.toPath(), fos);
		fos.close()

		log.info 'Transpilatation complete successfully. QoS for Orcha orchestrator copied into ' + xmlSpringContentInSrc
		
		// update temporary file
		xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + "main" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
		newFile = new File(xmlSpringContentInSrc);
		fos = new FileOutputStream(newFile)
		Files.copy(oldFile.toPath(), fos);
		fos.close()

		log.info 'Transpilatation complete successfully. QoS for Orcha orchestrator copied into ' + xmlSpringContentInSrc

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
	private propagateReceiveEvent(OrchaCodeVisitor orchaCodeParser){
		
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
						
						instrucNode.options != null &&
						
						
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
	 * Connect together mainly channels of Spring integration
	 * @param orchaCodeParser
	 */
	private void generateInputOutputChannelNames(OrchaCodeVisitor orchaCodeVisitor){
		
		List<InstructionNode> nodes = orchaCodeVisitor.findAllReceiveNodes()
		
		nodes.each { node ->
			
			node.inputName = node.instruction.springBean.name + '-InputChannel'
			node.outputName = node.instruction.springBean.name + "-OutputChannel"
			
		}
		
		List<InstructionNode> graphOfInstructions = orchaCodeVisitor.findAllNodes()
		
		graphOfInstructions.each{ node ->
			InstructionNode nextNode = orchaCodeVisitor.findNextRawNode(node)
			if(nextNode != null){
				nextNode.inputName = node.outputName
			}
		}
		
		nodes = orchaCodeVisitor.findAllReceiveNodesWithTheSameEvent()
		
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
							
				InstructionNode receiveNode = orchaCodeVisitor.findNextRawNode(node, alreadyDoneInstructions)
				alreadyDoneInstructions.add(receiveNode)
				
				InstructionNode nextToReceiveNode = orchaCodeVisitor.findNextRawNode(receiveNode)
				nextToReceiveNode.inputName = node.next.outputName
				
				node = node.next
				index++
			}
		}
		
		InstructionNode whenNode
		
		nodes = orchaCodeVisitor.findAllComputeNodes()
		nodes.each { node ->
			
			node.outputName = node.instruction.springBean.name + "Output"
			
			whenNode = node.next
			while(whenNode != null) {

				def orchaExpression = whenNode.instruction.variable
				
				List<String> applicationsNames = expressionParser.getApplicationsNamesInExpression(orchaExpression)
										
				String aggregatorName = ""
				for(String name: applicationsNames){
					aggregatorName = aggregatorName + name
				}
										
				whenNode.outputName = aggregatorName + "AggregatorInput"
				//whenNode.inputName = node.outputName
				whenNode = whenNode.next
			}
			
		}
		
		
		
		nodes = orchaCodeVisitor.findAllWhenNodesWithDifferentApplicationsInExpression()
		
		nodes.each { node ->
			
			def orchaExpression = node.instruction.variable
			
			List<String> applicationsNames = expressionParser.getApplicationsNamesInExpression(orchaExpression)
									
			String aggregatorName = ""
			for(String name: applicationsNames){
				aggregatorName = aggregatorName + name
			}
									
			node.inputName = aggregatorName + "AggregatorInput"
			node.outputName = aggregatorName + "AggregatorOutputTransformer"
				
			InstructionNode nextToWhenNode = orchaCodeVisitor.findNextRawNode(node)
			nextToWhenNode.inputName = node.outputName
		}
		
		// when  "selectBestVendor terminates condition price>1000"
		// send selectBestVendor.result to outputFile1
		// when  "selectBestVendor terminates condition price<=1000"
		// send selectBestVendor.result to outputFile2
		
		nodes = orchaCodeVisitor.findAllWhenNodesWithTheSameApplicationsInExpression()
		
		nodes.each { node ->
			
			rootNode = node
			
			node = node.next	// the first node is an empty node with only adjacent nodes
			
			String orchaExpressionForNode = node.instruction.variable
			
			List<String> applicationsNames = expressionParser.getApplicationsNamesInExpression(orchaExpressionForNode)
	
			String aggregatorName = ""
			for(String name: applicationsNames){
				aggregatorName = aggregatorName + name
			}
			
			rootNode.inputName = aggregatorName + "AggregatorInput"
			rootNode.outputName = aggregatorName + "AggregatorOutputTransformer"
	
			int index = 1
			
			while(node != null){
					
				orchaExpressionForNode = node.instruction.variable
					
				if(expressionParser.isFailExpression(node, orchaCodeVisitor.findAllNodes()) == true){
					
					String failChannel = expressionParser.failChannel(node)
					failChannel = failChannel  + "-output"
					
					List<InstructionNode> nextNodes = orchaCodeVisitor.findNextNode(node)
					nextNodes.each { nextNode ->
						nextNode.inputName = failChannel
					}
					
				} else if(expressionParser.isSeveralWhenWithSameApplicationsInExpression(node)){
					
					node.inputName = rootNode.inputName
					node.outputName = rootNode.inputName + "Route" + index
	
					whenNode = orchaCodeVisitor.findAdjacentNode(node)
				
					whenNode.inputName = node.outputName
					whenNode.outputName = rootNode.outputName + "Route" + index
					
					InstructionNode nextTowhenNode = orchaCodeVisitor.findNextRawNode(whenNode)
					nextTowhenNode.inputName = whenNode.outputName
							
					index++

				} else {
				
					node.inputName = aggregatorName + "AggregatorInput"
					node.outputName = aggregatorName + "AggregatorOutputTransformer"
				
					List<InstructionNode> nextNodes = orchaCodeVisitor.findNextNode(node)
					InstructionNode nextNode = nextNodes.getAt(0)
					nextNode.inputName = node.outputName
				}
				
				node = node.next
				
			}
		}
		
		
		InstructionNode node
		
		nodes = orchaCodeVisitor.findAllNodes()
		for(int i=nodes.size()-1; i>=0; i--){
			node = nodes.getAt(i)
			if(node.instruction.instruction == "when"){
				List<InstructionNode> previousNodes = orchaCodeVisitor.findAllRawPrecedingNodes(node)
				previousNodes.each { beforeWhenNode ->
					if(beforeWhenNode.instruction.instruction != "when"){
						// isn't a when node with a fails :
						// compute appli1
						// when "appli1 fails"
						String orchaExpression = node.instruction.variable
						if(orchaExpression == null){
							orchaExpression = node.next.instruction.variable
						}
						if(expressionParser.isComputeFailsInExpression(beforeWhenNode, orchaExpression) == false  && beforeWhenNode.next!=null && beforeWhenNode.next.next==null){
							beforeWhenNode.outputName = node.inputName
						}
					}
				}
			}
		}
		
	}
	
	private void generateGeneralContext(Document xmlSpringIntegration){
		
		Element rootElement = xmlSpringIntegration.getRootElement()

		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element element = new Element("logging-channel-adapter", namespace)
		element.setAttribute("id", "loggingChannel")
		element.setAttribute("level", "INFO")
		rootElement.addContent(element)
		
		namespace = Namespace.getNamespace("int-stream", "http://www.springframework.org/schema/integration/stream")
		
		element = new Element("stderr-channel-adapter", namespace)
		element.setAttribute("channel", "errorChannel")
		element.setAttribute("append-newline", "true")
		rootElement.addContent(element)
		
		namespace = rootElement.getNamespace()
		
		element = new Element("bean", namespace)
		element.setAttribute("id", "errorUnwrapper")
		element.setAttribute("class", "orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ErrorUnwrapper")
		rootElement.addContent(element)
		
		namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		element = new Element("channel", namespace)
		element.setAttribute("id", "recoveryChannel")
		rootElement.addContent(element)
		
		namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		element = new Element("transformer", namespace)
		element.setAttribute("input-channel", "recoveryChannel")
		element.setAttribute("output-channel", "loggingChannel")
		element.setAttribute("expression", "'Failure after many attemps for the message :' + payload.failedMessage.payload")
		rootElement.addContent(element)

	}

	private void exportToXML(Document document, File xmlFile){
		
		this.exportToXML(document, xmlFile, Format.PrettyFormat)
		
	}
	
	private void exportToXML(Document document, File xmlFile, Format format){
		
		org.jdom2.output.Format jdomFormat
		
		if(format == Format.CompactFormat){
			jdomFormat = org.jdom2.output.Format.getCompactFormat()
		} else if(format == Format.RawFormat){
			jdomFormat = org.jdom2.output.Format.getRawFormat()
		} else {
			jdomFormat = org.jdom2.output.Format.getPrettyFormat()
		}
		
		XMLOutputter xml = new XMLOutputter()
		xml.setFormat(jdomFormat)
		XMLOutputProcessor xmlProcessor = xml.getXMLOutputProcessor()
		FileWriter fw = new FileWriter(xmlFile)
		xmlProcessor.process(fw, jdomFormat, document)
		fw.close()

	}
	
	private void generateXMLForInstruction(InstructionNode instructionNode, File destinationDirectory, OrchaCodeVisitor orchaCodeParser, List<Instruction>alreadyDoneInstructions, Document xmlSpringIntegration){
		
		//List<Instruction> graphOfInstructions = orchaCodeParser.findAllNodes()
		
		Instruction instruction = instructionNode.instruction
			
		if(instruction.instruction == "receive"){
			
			if( alreadyDoneInstructions.contains(instruction) == false){

				String title = orchaCodeParser.getOrchaMetadata().getTitle()
				generateReceiveEventHandler(instructionNode, destinationDirectory, xmlSpringIntegration, title)
				
				if(instructionNode.next.instruction.instruction == "receive"){
					
					generateRouterForEventHandlers(orchaCodeParser, instructionNode, expressionParser, xmlSpringIntegration)
					
					InstructionNode node = instructionNode.next
					
					while(node != null){
						alreadyDoneInstructions.add(node.instruction)
						node = node.next
					}
				}
				
/*				if(instructionNode.next.instruction.instruction == "receive"){
					
					xmlEvent = generateRouterForEventHandlers(instructionNode)
					bufferedWriter.writeLine(xmlEvent.toString())
					
					InstructionNode node = instructionNode.next
					
					while(node != null){
						alreadyDoneInstructions.add(node.instruction)
						node = node.next
					}
				}*/

			}
			
		}else if(instruction.instruction == "compute"){
		
			String failChannel = expressionParser.failChannel(instructionNode)
			
			// when nodes
			List<InstructionNode> nodes = orchaCodeParser.findNextNode(instructionNode)
			
			// is a when node with a fails :
			// compute appli1
			// when "appli1 fails"
			boolean computeFails = (null != nodes.find { expressionParser.isComputeFailsInExpression(instructionNode, it.instruction.variable) })
			
			int sequenceSize
			int sequenceNumber
			
			nodes.each { whenNode ->	// when "(codeToBenchmark1 terminates condition == -1) and (codeToBenchmark2 terminates condition == 1)"
				// codeToBenchmark1 should be received first, then codeToBenchmark2. So a resequencer of messages is needed
				sequenceSize = expressionParser.getNumberOfApplicationsInExpression(whenNode.instruction.variable)
				sequenceNumber = expressionParser.getIndexOfApplicationInExpression(whenNode.instruction.variable, instructionNode.instruction.springBean.name)
				
			}

			String title = orchaCodeParser.getOrchaMetadata().getTitle()
			
			if(sequenceSize>1 && nodes.size()==1) {
				generateApplication(destinationDirectory, orchaCodeParser, instructionNode, title, sequenceNumber, sequenceSize, computeFails, failChannel, xmlSpringIntegration)
			} else {
				
				generateApplication(destinationDirectory, orchaCodeParser, instructionNode, title, computeFails, failChannel, xmlSpringIntegration)
			}
			
			if(nodes.size() > 1) {
				generateRouterForEventHandlers(orchaCodeParser, instructionNode, expressionParser, xmlSpringIntegration)
			}
			
		} else if(instruction.instruction == "when"){
			
			if(instructionNode.next.instruction.instruction == "when"){
				
				if(expressionParser.isSeveralWhenWithSameApplicationsInExpression(instructionNode.next)){
					
					RouterForAggregator routerForAggregator = new RouterForAggregator(xmlSpringIntegration)
					routerForAggregator.routerForAggregator(instructionNode)
					
				}
								
			} else {
								
				String orchaExpression = instruction.variable
			
				List<InstructionNode> precedingNodes = orchaCodeParser.findAllPrecedingNodes(instructionNode)
				
				if(precedingNodes.size()>0 && expressionParser.isComputeFailsInExpression(precedingNodes.getAt(0), orchaExpression)==false){

					String releaseExpression = expressionParser.releaseExpression(orchaExpression)
					String transformerExpression = expressionParser.aggregatorTransformerExpression(orchaExpression, instructionNode)
					boolean isMultipleArgumentsInExpression = expressionParser.isMultipleArgumentsInExpression(orchaExpression, instructionNode)
					List<String> applicationsNames = expressionParser.getApplicationsNamesInExpression(instructionNode.instruction.variable)
				
					Aggregator aggregator = new Aggregator(xmlSpringIntegration)
					aggregator.aggregate(instructionNode, releaseExpression, applicationsNames, transformerExpression, isMultipleArgumentsInExpression)
	
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
												
								String failedServiceName = expressionParser.failedServiceName(instructionNode)
								String failChannel = expressionParser.failChannel(instructionNode)
								
								Fail fail = new Fail(xmlSpringIntegration)
								fail.fail(instructionNode, failedServiceName, failChannel, errorExpression)
								
							}
						}
					}
				}
			}
		
		} else if(instruction.instruction == "send"){
		
			generateSendEventHandler(destinationDirectory, orchaCodeParser, instructionNode, xmlSpringIntegration)
		
		}

	}
	
	private void generateReceiveEventHandler(InstructionNode instructionNode, File destinationDirectory, Document xmlSpringIntegration, String title){
		
		def EventHandler eventHandler = instructionNode.instruction.springBean
		
		String filteringExpression = null
		
		if(instructionNode.instruction.condition != null) {
			filteringExpression = expressionParser.filteringExpression(instructionNode.instruction.condition)
		}
		
		InboundChannelAdapter inboundChannelAdapter = new InboundChannelAdapter(destinationDirectory, xmlSpringIntegration, filteringExpression)
		
		if(eventHandler.input!=null && eventHandler.input.adapter instanceof HttpAdapter){
				
			inboundChannelAdapter.http(instructionNode)
			
		} else if(eventHandler.input!=null && eventHandler.input.adapter instanceof InputFileAdapter){
		
			inboundChannelAdapter.file(instructionNode)
		
		} else if(eventHandler.input!=null && eventHandler.input.adapter instanceof MailReceiverAdapter){
		
			throw new OrchaCompilationException(eventHandler.toString() + " not supported yet.")
			
		} else if(eventHandler.input!=null && eventHandler.input.adapter instanceof MailSenderAdapter){
		
			throw new OrchaCompilationException(eventHandler.toString() + " not supported yet.")
			
		} else if(eventHandler.input!=null && eventHandler.input.adapter instanceof ComposeEventAdapter){
		
			throw new OrchaCompilationException(eventHandler.toString() + " not supported yet.")
			
		} else if(eventHandler.input!=null && eventHandler.input.adapter instanceof MessagingMiddlewareAdapter){
		
			inboundChannelAdapter.messagingMiddleware(instructionNode, title)
			
		} else {
			throw new OrchaCompilationException(eventHandler.toString() + " not supported yet.")
		}
	}
	
	private void generateApplication(File destinationDirectory, OrchaCodeVisitor orchaCodeParser, InstructionNode instructionNode, String title, int sequenceNumber, int sequenceSize, boolean computeFails, String failChannel, Document xmlSpringIntegration){
		
		ServiceActivator serviceActivator = new ServiceActivator(destinationDirectory, xmlSpringIntegration)
		
		if(instructionNode.instruction.springBean.input.adapter instanceof JavaServiceAdapter){
			
			boolean isScript = false
			serviceActivator.service(instructionNode, sequenceNumber, sequenceSize, computeFails, failChannel, isScript)
			
		} else if(instructionNode.instruction.springBean.input.adapter instanceof ScriptServiceAdapter){
		
			boolean isScript = true
			serviceActivator.service(instructionNode, sequenceNumber, sequenceSize, computeFails, failChannel, isScript)
			
		} else if(instructionNode.instruction.springBean.input.adapter instanceof OrchaServiceAdapter){
			
			this.generateRedirectInputEventToSendEventHandler(orchaCodeParser, instructionNode, xmlSpringIntegration)
			this.generateRedirectOutputEventToReceiveEventHandler(instructionNode, title,  xmlSpringIntegration)
	
		}
	}
	
	private void generateApplication(File destinationDirectory, OrchaCodeVisitor orchaCodeParser, InstructionNode instructionNode, String title, boolean computeFails, String failChannel, Document xmlSpringIntegration){
		
		ServiceActivator serviceActivator = new ServiceActivator(destinationDirectory, xmlSpringIntegration)
		
		if(instructionNode.instruction.springBean.input.adapter instanceof JavaServiceAdapter){
			
			boolean isScript = false
			serviceActivator.service(instructionNode, computeFails, failChannel, isScript)
			
		} else if(instructionNode.instruction.springBean.input.adapter instanceof ScriptServiceAdapter){
		
			boolean isScript = true
			serviceActivator.service(instructionNode, computeFails, failChannel, isScript)
			
		} else if(instructionNode.instruction.springBean.input.adapter instanceof OrchaServiceAdapter){
			
			this.generateRedirectInputEventToSendEventHandler(destinationDirectory, orchaCodeParser, instructionNode, xmlSpringIntegration)
			this.generateRedirectOutputEventToReceiveEventHandler(destinationDirectory, instructionNode, title, xmlSpringIntegration)
			
		}
		
/*		if(instructionNode.instruction.springBean.input.adapter instanceof MailSenderAdapter){
			
			def xml = new StreamingMarkupBuilder()
			xml.useDoubleQuotes = true
			def xmlEvent = xml.bind(connectors.mailSenderAdapter(instructionNode))
			bufferedWriter.writeLine(xmlEvent.toString())
			
		}
		
		if(instructionNode.instruction.springBean.output.adapter instanceof MailReceiverAdapter){
			
			def xml = new StreamingMarkupBuilder()
			xml.useDoubleQuotes = true
			def xmlEvent = xml.bind(connectors.mailReceiverAdapter(instructionNode))
			bufferedWriter.writeLine(xmlEvent.toString())
			
		}*/
		

	}
	
	private void generateRouterForEventHandlers(OrchaCodeVisitor orchaCodeParser, InstructionNode instructionNode, ExpressionParser expressionParser, Document xmlSpringIntegration){
		
		RouterForEventHandler routerForEventHandler = new RouterForEventHandler(orchaCodeParser, xmlSpringIntegration, expressionParser)
		routerForEventHandler.routerForEventHandler(instructionNode)
	
	}
	
	private void generateSendEventHandler(File destinationDirectory, OrchaCodeVisitor orchaCodeParser, InstructionNode instructionNode, Document xmlSpringIntegration){
		
		OutboundChannelAdapter outboundChannelAdapter = new OutboundChannelAdapter(destinationDirectory, orchaCodeParser, xmlSpringIntegration)
		
		def EventHandler eventHandler = instructionNode.instruction.springBean
		
		if(eventHandler.output.adapter instanceof DatabaseAdapter){
			
			throw new OrchaCompilationException(eventHandler.toString() + " not supported yet.")
			
		} else if(eventHandler.output.adapter instanceof OutputFileAdapter){
				
			outboundChannelAdapter.file(instructionNode)
			
		} else if(eventHandler.output.adapter instanceof MailSenderAdapter){
				
			throw new OrchaCompilationException(eventHandler.toString() + " not supported yet.")
			
		} else if(eventHandler.output.adapter instanceof MessagingMiddlewareAdapter){
		
			outboundChannelAdapter.messagingMiddleware(instructionNode)
			
		} else {
			throw new OrchaCompilationException(eventHandler.toString() + " not supported yet.")
		}
	}
	
	private void generateRedirectInputEventToSendEventHandler(File destinationDirectory, OrchaCodeVisitor orchaCodeParser, InstructionNode instructionNode, Document xmlSpringIntegration){
		
		OutboundChannelAdapter outboundChannelAdapter = new OutboundChannelAdapter(destinationDirectory, orchaCodeParser, xmlSpringIntegration)
		
		OrchaServiceAdapter orchaServiceAdapter = instructionNode.instruction.springBean.input.adapter
		
		if(orchaServiceAdapter instanceof DatabaseAdapter){
			
			throw new OrchaCompilationException(orchaServiceAdapter.toString() + " not supported yet.")
			
		} else if(orchaServiceAdapter instanceof InputFileAdapter){
			
			outboundChannelAdapter.file(instructionNode, orchaServiceAdapter.input)
			
		} else if(orchaServiceAdapter instanceof OrchaServiceAdapter){
			
			List<InstructionNode> receiveNodes = orchaCodeParser.findAllPrecedingReceiveNodesWithTheSameEvent(instructionNode)
			String partitionKeyExpression = expressionParser.partitionKeyExpression(receiveNodes.get(0))
			outboundChannelAdapter.messagingMiddleware(instructionNode, partitionKeyExpression)
			
		} else if(orchaServiceAdapter instanceof MailSenderAdapter){
				
			throw new OrchaCompilationException(orchaServiceAdapter.toString() + " not supported yet.")
			
		} else {
			throw new OrchaCompilationException(orchaServiceAdapter.toString() + " not supported yet.")
		}
	}

	private void generateRedirectOutputEventToReceiveEventHandler(File destinationDirectory, InstructionNode instructionNode, String title, Document xmlSpringIntegration){
		
		String filteringExpression = null
		
		if(instructionNode.instruction.condition != null) {
			filteringExpression = expressionParser.filteringExpression(instructionNode.instruction.condition)
		}
		InboundChannelAdapter inboundChannelAdapter = new InboundChannelAdapter(destinationDirectory, xmlSpringIntegration, filteringExpression)
		
		OrchaServiceAdapter orchaServiceAdapter = instructionNode.instruction.springBean.output.adapter
		
		if(orchaServiceAdapter instanceof DatabaseAdapter){
			
			throw new OrchaCompilationException(orchaServiceAdapter.toString() + " not supported yet.")
			
		} else if(orchaServiceAdapter instanceof InputFileAdapter){
			
			inboundChannelAdapter.file(instructionNode)
			
		} else if(orchaServiceAdapter instanceof OrchaServiceAdapter){
			
			inboundChannelAdapter.messagingMiddleware(instructionNode, title)
			
		} else if(orchaServiceAdapter instanceof MailSenderAdapter){
				
			throw new OrchaCompilationException(orchaServiceAdapter.toString() + " not supported yet.")
			
		} else {
			throw new OrchaCompilationException(orchaServiceAdapter.toString() + " not supported yet.")
		}
	}

}
