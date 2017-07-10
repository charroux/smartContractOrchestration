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
import org.jdom2.xpath.XPathExpression
import org.jdom2.xpath.XPathFactory
import org.jdom2.filter.Filters

class JDomEssai {
	
	public static void main(String[] a){
		
		//String xmlSchema = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "note.xsd"
		String xmlSchema = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "maven-4.0.0.xsd"
		//String xmlFile = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "note.xml"
		
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
		
/*		List<Content> contents = doc.getContent()
		for(Content content: contents){
			println content
			if(content instanceof Element){
				Element element = (Element)content
				java.util.List<Element> children =element.getChildren()
				for(Element e: children){
					println e
				}
			}
		}*/
		
	}

}
