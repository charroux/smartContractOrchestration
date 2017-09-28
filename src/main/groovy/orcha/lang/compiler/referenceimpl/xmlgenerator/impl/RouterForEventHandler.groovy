package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import orcha.lang.compiler.qualityOfService.QueueOption
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode

class RouterForEventHandler implements QoS{
	
	Document xmlSpringIntegration
	
	public RouterForEventHandler(Document xmlSpringIntegration) {
		super();
		this.xmlSpringIntegration = xmlSpringIntegration;
	}
	
	public void routerForEventHandler(InstructionNode instructionNode){
			
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Instruction instruction = (Instruction)instructionNode.instruction
		
		def inputChannel = instructionNode.outputName		
			
		Element router = new Element("recipient-list-router", namespace)
		router.setAttribute("id", 'router-' + inputChannel + '-id')
		router.setAttribute("input-channel", inputChannel)
		rootElement.addContent(router)
		
		InstructionNode node = instructionNode.next
		int i=0
		boolean  defaultChannel = true
				
		while(node != null){
					
			Instruction nextInstruction = node.instruction
					
			String channelName = node.outputName						
									
			Element recipient = new Element("recipient", namespace)
			recipient.setAttribute("channel", channelName)
					
			if(nextInstruction.condition != null){

				String selectorExpression = nextInstruction.condition.replaceFirst(nextInstruction.variable,"payload")
				recipient.setAttribute("selector-expression", selectorExpression)
												
			} else {
				defaultChannel = false
			}
					
			router.addContent(recipient)
					
			node = node.next
			i++
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
