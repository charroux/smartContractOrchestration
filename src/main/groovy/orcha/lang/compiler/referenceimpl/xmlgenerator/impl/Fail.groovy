package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode

class Fail implements Bean{
	
	Document xmlSpringIntegration
	
	public Fail(Document xmlSpringIntegration) {
		super();
		this.xmlSpringIntegration = xmlSpringIntegration;
	}
	
	public void fail(InstructionNode instructionNode, String failedServiceName, String failChannel, String errorExpression){
		
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")		
		
		Instruction instruction = instructionNode.instruction
				 
		def outputChannel = instructionNode.inputName + "AggregatorOutput"
		
		Element channel = new Element("channel", namespace)
		channel.setAttribute("id", failChannel+"-id")
		rootElement.addContent(channel)
		
		Element transformer = new Element("transformer", namespace)
		transformer.setAttribute("input-channel", failChannel+"-id")
		transformer.setAttribute("output-channel", failChannel+"-errorUnwrapper-output")
		transformer.setAttribute("expression", "@errorUnwrapper.transform(payload)")		
		rootElement.addContent(transformer)
		
		transformer = new Element("transformer", namespace)
		transformer.setAttribute("id", "transformer-"+failChannel+"-output-id")
		transformer.setAttribute("input-channel", failChannel+"-errorUnwrapper-output")
		transformer.setAttribute("output-channel", failChannel+"-ErrorToApplication-output")
		transformer.setAttribute("method", "transform")
		rootElement.addContent(transformer)
		
		def properties = [application: failedServiceName]
		Element beanElement = beanWithRef("orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ErrorToApplicationTransformer", properties)
		rootElement.addContent(bean)		
		
		transformer = new Element("transformer", namespace)
		transformer.setAttribute("input-channel", failChannel+"-ErrorToApplication-output")
		transformer.setAttribute("output-channel", failChannel+"-output")
		transformer.setAttribute("expression", errorExpression)		
		rootElement.addContent(transformer)
			
	}

}
