package orcha.lang.contract.impl

import java.io.File

import javax.xml.XMLConstants
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.configuration.Application
import orcha.lang.contract.ContractGenerator
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.xpath.XPathExpression
import org.jdom2.xpath.XPathFactory

import groovy.util.logging.Slf4j

import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaderJDOMFactory
import org.jdom2.input.sax.XMLReaderSchemaFactory
import org.jdom2.output.XMLOutputter
import org.jdom2.output.support.XMLOutputProcessor

@Slf4j
class ContractGeneratorImpl implements ContractGenerator{

	Document document
	String xmlContract
	Namespace namespace
	
	public ContractGeneratorImpl() {
		super();
		
		//URL xmlContractSchemaUrl = new URL("http://orchalang.com/schema/contract.xsd")
		
		String contractXmlSchema = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "contract.xsd"
		
		xmlContract = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "contract.xml"
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(new File(contractXmlSchema));
		//Schema schema = schemaFactory.newSchema(xmlContractSchemaUrl);
		 
		XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);
		SAXBuilder sb = new SAXBuilder(factory);
		document = sb.build(new File(xmlContract))
		
		namespace = document.getRootElement().getNamespace()
		
	}
	
	@Override
	public void generateAll(OrchaCodeVisitor orchaCodeVisitor) {
		
		updateCommitments(orchaCodeVisitor)
		
	}
	
	@Override
	public void exportToXML(File xmlFile){
		
		this.exportToXML(xmlFile, Format.PrettyFormat)
		
	}
	
	@Override
	public void exportToXML(File xmlFile, Format format){
		
		org.jdom2.output.Format jdomFormat
		
		if(format == Format.CompactFormat){
			jdomFormat = org.jdom2.output.Format.getCompactFormat()
		} else if(format == Format.RawFormat){
			jdomFormat = org.jdom2.output.Format.getRawFormat()
		} else {
			jdomFormat = org.jdom2.output.Format.getPrettyFormat()
		}
		
		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(jdomFormat);
		XMLOutputProcessor xmlProcessor = xml.getXMLOutputProcessor()
		FileWriter fw = new FileWriter(xmlFile);
		xmlProcessor.process(fw, jdomFormat, document)
		
	}
	
	@Override
	public void updateCommitments(OrchaCodeVisitor orchaCodeVisitor){
		
		XPathFactory xFactory = XPathFactory.instance()
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'commitments']/*[local-name() = 'commitment']/*[local-name() = 'name']", Filters.element())
		List<Element> elements = expr.evaluate(document)
		def names = []
		for(Element element: elements){
			names.add(element.getValue())
		}

		List<InstructionNode> computeNodes = orchaCodeVisitor.findAllComputeNodes()
		
		computeNodes.each { computeNode ->
			Application application = (Application)computeNode.instruction.springBean
			if(names.contains(application.name) == false){
				elements = xFactory.compile("//*[local-name() = 'commitments']", Filters.element()).evaluate(document)
				Element commitments = elements.get(0)	// commitments
				Element element = new Element("commitment", namespace)
				if(application.name != null){
					element.addContent(new Element("name", namespace).addContent(application.name))
				}
				if(application.description != null){
					element.addContent(new Element("description", namespace).addContent(application.description))
				} 
				if(application.specifications){
					element.addContent(new Element("specifications", namespace).addContent(application.specifications))
				}
				commitments.addContent(element)
			}
		}
		
		log.info "Commitments updated in XML document"
				
	}

}
