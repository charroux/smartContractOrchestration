package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Element
import org.jdom2.Namespace
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.qualityOfService.EventSourcingOption

trait Transformer implements Bean, QoS{

	public Element objectToString() {		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		Element element = new Element("object-to-string-transformer", namespace)
		return element
	}
	
	public Element objectToJson() {
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")		
		Element element = new Element("object-to-json-transformer", namespace)		
		return element
	}
	
	public Element jsonToObject(String type) {
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element element = new Element("json-to-object-transformer", namespace)
		element.setAttribute("type", type)
		
		return element
	}
	
	public Element objectToApplicationTransformer(InstructionNode instructionNode){
		
		Instruction instruction = instructionNode.instruction
		
		def applicationName = instruction.springBean.name
		def outputServiceChannel = applicationName + "ServiceAcivatorOutput"
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element transformer = new Element("transformer", namespace)
		transformer.setAttribute("id", "transformer-"+outputServiceChannel+"-id")
		transformer.setAttribute("input-channel", outputServiceChannel)
		transformer.setAttribute("output-channel", instructionNode.outputName)
		transformer.setAttribute("method", "transform")
		
		def properties = [application: applicationName]
		Element beanElement = beanWithRef("orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer", properties)
		
		transformer.addContent(beanElement)
		
		if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){
			if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.after || instructionNode.options.eventSourcing.joinPoint==JoinPoint.beforeAndAfter){
				Element adviceChain = new Element("request-handler-advice-chain", namespace)
				transformer.addContent(adviceChain)
				Element eventSourcingElement = eventSourcing(instructionNode.options.eventSourcing)
				adviceChain.addContent(adviceChain)
			}
		}
		
		return transformer
		
	}
	
	public Element applicationsListToObjectsListTransformer(InstructionNode instructionNode){
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		def outputChannel = instructionNode.inputName + "AggregatorOutput"
		
		Element transformer = new Element("transformer", namespace)
		transformer.setAttribute("id", "transformer-"+outputChannel+"-id")
		transformer.setAttribute("input-channel", outputChannel)
		transformer.setAttribute("output-channel", instructionNode.outputName)
		transformer.setAttribute("method", "transform")
		
		def properties = [:]
		Element beanElement = beanWithValue("orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ApplicationsListToObjectsListTransformer", properties)
		
		transformer.addContent(beanElement)
		
		return transform
		
	}
	
	public Element applicationToObjectTransformer(InstructionNode instructionNode){
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		def outputChannel = instructionNode.inputName + "AggregatorOutput"
		
		Element transformer = new Element("transformer", namespace)
		transformer.setAttribute("id", "transformer-"+outputChannel+"-id")
		transformer.setAttribute("input-channel", outputChannel)
		transformer.setAttribute("output-channel", instructionNode.outputName)
		transformer.setAttribute("method", "transform")
		
		def properties = [:]
		Element beanElement = beanWithValue("orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ApplicationToObjectTransformer", properties)
		
		transformer.addContent(beanElement)
		
		return transformer
		
	}

}
