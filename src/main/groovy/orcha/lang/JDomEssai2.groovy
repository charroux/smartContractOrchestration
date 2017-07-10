package orcha.lang

import java.io.IOException;
import java.util.List;
import org.jdom2.Content
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jdom2.output.Format

class JDomEssai2 {

	static main(args) {
	
		//String xmlFile = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "feeds.xml"
		String xmlFile = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "contract.xml"
		//String xmlFile = "http://feeds.bbci.co.uk/news/technology/rss.xml?edition=int";
		
		// read the XML into a JDOM2 document.
		SAXBuilder jdomBuilder = new SAXBuilder();
		Document jdomDocument = jdomBuilder.build(xmlFile);
		
		List<Content> contents = jdomDocument.getContent()
		for(Content content: contents){
			println content
			if(content instanceof Element){
				Element element = (Element)content
				java.util.List<Element> children =element.getChildren()
				for(Element e: children){
					println e
				}
			}
		}
		
		println '---------------------'
  
		// use the default implementation
		XPathFactory xFactory = XPathFactory.instance();
		// System.out.println(xFactory.getClass());
  
		// select all links
		//XPathExpression<Element> expr = xFactory.compile("//version[namespace()='http://orchalang.com/schema']", Filters.element());
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'commitments']", Filters.element())
		//XPathExpression<Element> expr = xFactory.compile("//contractModelVersion", Filters.element());
		List<Element> elements = expr.evaluate(jdomDocument)
		if(elements.size() == 1){
			Element commitments = elements.get(0)	// commitments
			Element element = new Element("commitment")
			element.addContent(new Element("name").addContent("essai"))
			commitments.addContent(element)
		}
		
		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(Format.getPrettyFormat());
		System.out.println(xml.outputString(jdomDocument));
		
		
	}

}
