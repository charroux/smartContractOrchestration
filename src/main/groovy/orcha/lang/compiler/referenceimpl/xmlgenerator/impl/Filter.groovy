package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Element
import org.jdom2.Namespace

trait Filter {
	
	public Element filter(String expression) {
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element element = new Element("filter", namespace)
		element.setAttribute("expression", expression)
		
		return element
	}

}
