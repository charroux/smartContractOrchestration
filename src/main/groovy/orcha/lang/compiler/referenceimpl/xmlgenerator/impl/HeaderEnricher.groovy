package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Element
import org.jdom2.Namespace

trait HeaderEnricher {
	
	public Element headerEnricher(String name, String expression) {
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element element = new Element("header-enricher", namespace)
		
		Element header = new Element("header", namespace)
		element.addContent(header)
		
		header.setAttribute("name", name)
		header.setAttribute("expression", expression)
		
		return element
	}

	public Element headerEnricher(String inputChannel, String outputChannel, String name, String expression) {
		
		Element element = this.headerEnricher(name, expression)		
		element.setAttribute("input-channel", inputChannel)
		element.setAttribute("output-channel", outputChannel)
		
		return element
	}

}
