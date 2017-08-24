package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Element
import org.jdom2.Namespace
import orcha.lang.compiler.qualityOfService.QueueOption

trait Chain {
	
	public Element chain(String inputChannel, String outputChannel) {
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element element = new Element("chain", namespace)
		
		element.setAttribute("input-channel", inputChannel)
		element.setAttribute("output-channel", outputChannel)
		
		return element
	}
	
	public Element chain(String id, String inputChannel, String outputChannel) {
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element element = this.chain(inputChannel, outputChannel)
		
		element.setAttribute("id", id)
		
		return element
	}

}
