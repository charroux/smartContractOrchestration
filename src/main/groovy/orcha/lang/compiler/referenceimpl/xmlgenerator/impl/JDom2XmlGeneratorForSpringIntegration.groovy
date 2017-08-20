package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import java.io.File
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.input.SAXBuilder
import org.jdom2.output.XMLOutputter
import org.jdom2.output.support.XMLOutputProcessor
import org.springframework.beans.factory.annotation.Autowired

import groovy.util.logging.Slf4j
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServicesOptions
import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.referenceimpl.xmlgenerator.XmlGenerator
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.contract.ContractGenerator.Format

@Slf4j
class JDom2XmlGeneratorForSpringIntegration implements XmlGenerator{

	@Autowired
	ExpressionParser expressionParser
	
	@Autowired
	QualityOfService qualityOfService
	
	@Override
	public void generate(OrchaCodeVisitor orchaCodeParser, File xmlSpringContextFile, File xmlQoSSpringContextFile) {
		
		this.propagateReceiveEvent(orchaCodeParser)
		
		// Connect together mainly channels of Spring integration
		this.generateInputOutputChannelNames(orchaCodeParser)
		
		qualityOfService.setQualityOfServiceToInstructions(orchaCodeParser)
		
		//this.resumeAtStoppingPoint(orchaCodeParser)
		
		BufferedWriter bufferedWriterSpringContext = new BufferedWriter(new FileWriter(xmlSpringContextFile))
		bufferedWriterSpringContext.writeLine('<beans  xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:stream="http://www.springframework.org/schema/integration/stream" xmlns:int="http://www.springframework.org/schema/integration" xmlns:int-mail="http://www.springframework.org/schema/integration/mail" xmlns:util="http://www.springframework.org/schema/util" xmlns:int-file="http://www.springframework.org/schema/integration/file" xmlns:int-stream="http://www.springframework.org/schema/integration/stream" xmlns:int-http="http://www.springframework.org/schema/integration/http" xmlns:int-groovy="http://www.springframework.org/schema/integration/groovy" xmlns:context="http://www.springframework.org/schema/context" xmlns:task="http://www.springframework.org/schema/task" xmlns:jdbc="http://www.springframework.org/schema/jdbc" xmlns:int-jdbc="http://www.springframework.org/schema/integration/jdbc" xmlns:int-script="http://www.springframework.org/schema/integration/scripting" xmlns:int-event="http://www.springframework.org/schema/integration/event" xmlns:int-amqp="http://www.springframework.org/schema/integration/amqp" xmlns:rabbit="http://www.springframework.org/schema/rabbit" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task.xsd http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration-4.3.xsd http://www.springframework.org/schema/integration/mail http://www.springframework.org/schema/integration/mail/spring-integration-mail-4.3.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd http://www.springframework.org/schema/integration/file http://www.springframework.org/schema/integration/file/spring-integration-file-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd http://www.springframework.org/schema/integration/jdbc http://www.springframework.org/schema/integration/jdbc/spring-integration-jdbc-4.3.xsd http://www.springframework.org/schema/jdbc http://www.springframework.org/schema/jdbc/spring-jdbc-4.3.xsd http://www.springframework.org/schema/integration/http http://www.springframework.org/schema/integration/http/spring-integration-http-4.3.xsd http://www.springframework.org/schema/integration/groovy http://www.springframework.org/schema/integration/groovy/spring-integration-groovy-4.3.xsd http://www.springframework.org/schema/integration/scripting http://www.springframework.org/schema/integration/scripting/spring-integration-scripting-4.3.xsd http://www.springframework.org/schema/integration/event http://www.springframework.org/schema/integration/event/spring-integration-event-4.3.xsd http://www.springframework.org/schema/rabbit http://www.springframework.org/schema/rabbit/spring-rabbit.xsd http://www.springframework.org/schema/integration/amqp http://www.springframework.org/schema/integration/amqp/spring-integration-amqp-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd">')
		bufferedWriterSpringContext.writeLine('</beans>')
		bufferedWriterSpringContext.flush()
		bufferedWriterSpringContext.close()
		
		SAXBuilder builder = new SAXBuilder();
		Document xmlSpringContext = builder.build(xmlSpringContextFile);
 
		generateGeneralContext(xmlSpringContext)
		
		this.exportToXML(xmlSpringContext, xmlSpringContextFile)
		
		//def xmlEvent = generateGeneralContext()
		//bufferedWriterSpringContext.writeLine(xmlEvent.toString())
		
		BufferedWriter bufferedWriterQoSSpringContext = new BufferedWriter(new FileWriter(xmlQoSSpringContextFile))
		bufferedWriterQoSSpringContext.writeLine('<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:stream="http://www.springframework.org/schema/integration/stream" xmlns:aop="http://www.springframework.org/schema/aop" xmlns:int="http://www.springframework.org/schema/integration" xmlns:int-file="http://www.springframework.org/schema/integration/file" xmlns:int-stream="http://www.springframework.org/schema/integration/stream" xmlns:int-http="http://www.springframework.org/schema/integration/http" xmlns:int-groovy="http://www.springframework.org/schema/integration/groovy" xmlns:context="http://www.springframework.org/schema/context" xmlns:task="http://www.springframework.org/schema/task" xmlns:jdbc="http://www.springframework.org/schema/jdbc" xmlns:int-jdbc="http://www.springframework.org/schema/integration/jdbc" xmlns:int-script="http://www.springframework.org/schema/integration/scripting" xmlns:int-event="http://www.springframework.org/schema/integration/event" xmlns:int-amqp="http://www.springframework.org/schema/integration/amqp" xmlns:rabbit="http://www.springframework.org/schema/rabbit" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task.xsd http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration-4.3.xsd http://www.springframework.org/schema/integration/file http://www.springframework.org/schema/integration/file/spring-integration-file-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd http://www.springframework.org/schema/integration/jdbc http://www.springframework.org/schema/integration/jdbc/spring-integration-jdbc-4.3.xsd http://www.springframework.org/schema/jdbc http://www.springframework.org/schema/jdbc/spring-jdbc-4.3.xsd http://www.springframework.org/schema/integration/http http://www.springframework.org/schema/integration/http/spring-integration-http-4.3.xsd http://www.springframework.org/schema/integration/groovy http://www.springframework.org/schema/integration/groovy/spring-integration-groovy-4.3.xsd http://www.springframework.org/schema/integration/scripting http://www.springframework.org/schema/integration/scripting/spring-integration-scripting-4.3.xsd http://www.springframework.org/schema/integration/event http://www.springframework.org/schema/integration/event/spring-integration-event-4.3.xsd http://www.springframework.org/schema/rabbit http://www.springframework.org/schema/rabbit/spring-rabbit.xsd http://www.springframework.org/schema/integration/amqp http://www.springframework.org/schema/integration/amqp/spring-integration-amqp-4.3.xsd http://www.springframework.org/schema/integration/stream http://www.springframework.org/schema/integration/stream/spring-integration-stream-4.3.xsd">')
		bufferedWriterQoSSpringContext.writeLine('</beans>')
		bufferedWriterQoSSpringContext.flush()
		bufferedWriterQoSSpringContext.close()
		
/*		def alreadyDoneInstructions = []
		
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
*/		
		
		
		// usefull to write line by line
		/*String xmlContext = xmlSpringContextFile.text
		String springContexteAsText = XmlUtil.serialize(xmlContext)
		xmlSpringContextFile.withWriter('utf-8') { writer ->
			writer.writeLine springContexteAsText
			writer.close()
		}*/
		
		log.info 'Transpilatation complete successfully. Orcha orchestrator generated into ' + xmlSpringContextFile.getAbsolutePath()
		
		
		// usefull to write line by line
		/*xmlContext = xmlQoSSpringContextFile.text
		springContexteAsText = XmlUtil.serialize(xmlContext)
		xmlQoSSpringContextFile.withWriter('utf-8') { writer ->
			writer.writeLine springContexteAsText
			writer.close()
		}*/
		
		log.info 'Transpilatation complete successfully. QoS for Orcha orchestrator generated into ' + xmlQoSSpringContextFile.getAbsolutePath()
		// TODO Auto-generated method stub
		
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
		
		nodes = orchaCodeVisitor.findAllComputeNodes()
		nodes.each { node ->
			node.outputName = node.instruction.springBean.name + "Output"
		}
		
		nodes = orchaCodeVisitor.findAllWhenNodesWithDifferentApplicationsInExpression()
		
		nodes.each { node ->
			
			def orchaExpression = node.instruction.variable
			
			List<String> applicationsNames = expressionParser.getApplicationsNamesInExpression(orchaExpression, graphOfInstructions)
									
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
					
				if(expressionParser.isFailExpression(node, orchaCodeVisitor.findAllNodes()) == true){
					
					String failChannel = expressionParser.failChannel(node, graphOfInstructions)
					failChannel = failChannel  + "-output"
					
					List<InstructionNode> nextNodes = orchaCodeVisitor.findNextNode(node)
					nextNodes.each { nextNode ->
						nextNode.inputName = failChannel
					}
					
				} else if(expressionParser.isSeveralWhenWithSameApplicationsInExpression(node)){
					
					node.inputName = rootNode.inputName
					node.outputName = rootNode.inputName + "Route" + index
	
					InstructionNode whenNode = orchaCodeVisitor.findAdjacentNode(node)
				
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
						if(expressionParser.isComputeFailsInExpression(beforeWhenNode, orchaExpression) == false){
							beforeWhenNode.outputName = node.inputName
						}
					}
				}
			}
		}
		
	}
	
	private void generateGeneralContext(Document xmlSpringContext){
		
		Element rootElement = xmlSpringContext.getRootElement();
		
		Namespace namespace = rootElement.getNamespace()
		
		Element element = new Element("logging-channel-adapter", namespace)
		rootElement.addContent(element)
		
		/*def xml = new StreamingMarkupBuilder()
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
		}*/
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
		
		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(jdomFormat);
		XMLOutputProcessor xmlProcessor = xml.getXMLOutputProcessor()
		FileWriter fw = new FileWriter(xmlFile);
		xmlProcessor.process(fw, jdomFormat, document)
		
	}
}
