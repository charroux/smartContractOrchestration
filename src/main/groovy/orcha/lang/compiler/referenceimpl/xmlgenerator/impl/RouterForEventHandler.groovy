package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import orcha.lang.compiler.qualityOfService.QueueOption
import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor

import java.util.List

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.springframework.beans.factory.annotation.Autowired

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode

class RouterForEventHandler implements QoS, Chain, HeaderEnricher{
	
	OrchaCodeVisitor orchaCodeParser
	ExpressionParser expressionParser
	Document xmlSpringIntegration
	
	public RouterForEventHandler(OrchaCodeVisitor orchaCodeParser, Document xmlSpringIntegration, ExpressionParser expressionParser) {
		super();
		this.orchaCodeParser = orchaCodeParser
		this.xmlSpringIntegration = xmlSpringIntegration;
		this.expressionParser = expressionParser
	}
	
	public void routerForEventHandler(InstructionNode instructionNode){
		
		InstructionNode node = instructionNode.next
		
		// if at least one instruction is followed by a compute instruction implemented by an Orcha program
		int numberOfNonOrchaApplications = 0 
		while(node!=null) {
			if(orchaCodeParser.findNextNode(node).get(0).instruction.springBean.language.equalsIgnoreCase("Orcha") == false) {
				numberOfNonOrchaApplications++
			}
			node = node.next
		}
		
		if(numberOfNonOrchaApplications >= 1) {
			this.router(instructionNode)
		}
		
	}
	
	private void router(InstructionNode instructionNode){
		
		String selectorOrchaExpression = expressionParser.partitonFilteringExpression(instructionNode)
		
		InstructionNode node = instructionNode.next
		
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Instruction instruction = (Instruction)instructionNode.instruction
		
		def inputChannel = instructionNode.outputName		
			
		Element router = new Element("recipient-list-router", namespace)
		router.setAttribute("id", 'router-' + inputChannel + '-id')
		router.setAttribute("input-channel", inputChannel)
		rootElement.addContent(router)
		
		node = instructionNode.next
		//int i=0
		boolean  defaultChannel = true
		boolean orchaProgramFound = false
		
		while(node != null){
			
			def springBean = orchaCodeParser.findNextNode(node).get(0).instruction.springBean
			 		
			if(springBean.language.equalsIgnoreCase("Orcha")==false || (springBean.language.equalsIgnoreCase("Orcha")==true && orchaProgramFound==false)) {
				
				orchaProgramFound = true
				
				Instruction nextInstruction = node.instruction
				
				String channelName = node.outputName
				
				Element recipient = new Element("recipient", namespace)
		
				int sequenceSize = expressionParser.getNumberOfApplicationsInExpression(node.instruction.variable)
				
				if(sequenceSize > 1) {
		
					recipient.setAttribute("channel", channelName + "Sequence")
					
					int sequenceNumber = expressionParser.getIndexOfApplicationInExpression(node.instruction.variable, instructionNode.instruction.springBean.name)
					Element chain = chain("sequenceNumber-" + channelName + "-id", channelName + "Sequence", channelName)
					Element header = headerEnricher("sequenceSize", sequenceSize.toString())
					chain.addContent(header)
					header = headerEnricher("sequenceNumber", sequenceNumber.toString())
					chain.addContent(header)
					rootElement.addContent(chain)
				} else {
					recipient.setAttribute("channel", channelName)
				}
				
				
				if(nextInstruction.condition != null){
		
					if(springBean.language.equalsIgnoreCase("Orcha") == true) {
						recipient.setAttribute("selector-expression", selectorOrchaExpression)
					} else {
						String selectorExpression =  expressionParser.filteringExpression(nextInstruction.condition)
						recipient.setAttribute("selector-expression", selectorExpression)
					}
																		
				} else {
					defaultChannel = false
				}
						
				router.addContent(recipient)
				
			}
			
			node = node.next
			
		}
				
		if(defaultChannel == true){
			Element recipient = new Element("recipient", namespace)
			recipient.setAttribute("channel", "loggingChannel")
			router.addContent(recipient)
		}
		
		node = instructionNode.next
			
		while(node != null){
	
			Instruction nextInstruction = node.instruction
			
			String channelName = node.outputName
			
			if(node.options != null){
				Element queueElement = queue(channelName, node.options.queue)
				rootElement.addContent(queueElement)
			}
						
			node = node.next
			
		}
		

	
	}


}
