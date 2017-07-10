package orcha.lang

import javax.xml.XMLConstants
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import org.jdom2.Content
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaderJDOMFactory
import org.jdom2.input.sax.XMLReaderSchemaFactory
import org.jdom2.input.sax.XMLReaderXSDFactory
import org.jdom2.output.XMLOutputter
import org.jdom2.xpath.XPathExpression
import org.jdom2.xpath.XPathFactory
import org.jdom2.filter.Filters
import org.jdom2.output.Format

class JDomEssai {
	
	public static void main(String[] a){
		
		String xmlSchema = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "contract.xsd"
		
		String xmlFile = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "contract.xml"
		
		//String xmlSchema = "note.xsd"
		//String xmlFile = "note.xml"
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		//URL url = new URL("http://orchalang.com/schema/note.xsd")
		//URL urlSchema = new URL("http://maven.apache.org/maven-v4_0_0.xsd")
		//Schema schema = schemaFactory.newSchema(urlSchema);
		Schema schema = schemaFactory.newSchema(new File(xmlSchema));
		 
		println schema
		 
		//URL urlFile = new URL("https://github.com/apache/commons-dbcp/blob/master/pom.xml")
		
		XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);
		SAXBuilder sb = new SAXBuilder(factory);
		Document doc = sb.build(new File(xmlFile));
		//Document doc = sb.build(urlFile);
		System.out.println(doc.getRootElement().getName());
		
		// use the default implementation
		XPathFactory xFactory = XPathFactory.instance();
		// System.out.println(xFactory.getClass());
  
		// select all links
		//XPathExpression<Element> expr = xFactory.compile("//version[namespace()='http://orchalang.com/schema']", Filters.element());
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'commitments']", Filters.element())
		//XPathExpression<Element> expr = xFactory.compile("//contractModelVersion", Filters.element());
		List<Element> elements = expr.evaluate(doc)
		if(elements.size() == 1){
			Element commitments = elements.get(0)	// commitments
			Element element = new Element("commitment")
			element.addContent(new Element("name").addContent("essai"))
			commitments.addContent(element)
		}
		
		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(Format.getPrettyFormat());
		System.out.println(xml.outputString(doc));
		
	}

}
