package orcha.lang.contract.impl

import java.io.File

import javax.xml.XMLConstants
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaMetadata
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.configuration.Application
import orcha.lang.contract.ContractGenerator
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.xpath.XPathExpression
import org.jdom2.xpath.XPathFactory
import org.springframework.beans.factory.annotation.Autowired

import groovy.util.logging.Slf4j

import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaderJDOMFactory
import org.jdom2.input.sax.XMLReaderSchemaFactory
import org.jdom2.output.XMLOutputter
import org.jdom2.output.support.XMLOutputProcessor

@Slf4j
class ContractGeneratorImpl implements ContractGenerator{
	
	@Autowired
	QualityOfService qualityOfService

	Document document
	String xmlContract
	Namespace namespace
	
	public ContractGeneratorImpl() {
		super();
		
		String contractXmlSchema = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "contract.xsd"
		
		xmlContract = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "contract.xml"
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(new File(contractXmlSchema));
		
		XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);
		SAXBuilder sb = new SAXBuilder(factory);
		document = sb.build(new File(xmlContract))
		
		namespace = document.getRootElement().getNamespace()
		
	}
	
	@Override
	public void generateAll(OrchaCodeVisitor orchaCodeVisitor) {
		
		this.updateRequirements(orchaCodeVisitor)
		
		this.updateCommitments(orchaCodeVisitor)
		
		this.updateQualityOfServices(orchaCodeVisitor)
		
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

	@Override
	public void updateRequirements(OrchaCodeVisitor orchaCodeVisitor){
		
		OrchaMetadata orchaMetadata = orchaCodeVisitor.getOrchaMetadata()
		
		XPathFactory xFactory = XPathFactory.instance()
		
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'domain']/*[local-name() = 'name']", Filters.element())
		List<Element> elements = expr.evaluate(document)		
		Element element = elements.getAt(0)
				
		String metadata = orchaMetadata.getDomain() 
		if(metadata != null){
			element.setText(metadata)
		}
		
		expr = xFactory.compile("//*[local-name() = 'title']", Filters.element())
		elements = expr.evaluate(document)
		element = elements.getAt(0)
				 
		metadata = orchaMetadata.getTitle()
		if(metadata != null){
			element.setText(metadata)
		}
		
		expr = xFactory.compile("//*[local-name() = 'description']", Filters.element())
		elements = expr.evaluate(document)
		element = elements.getAt(0)
		
		metadata = orchaMetadata.getDescription()
		if(metadata != null){
			element.setText(metadata)
		}
						
		expr = xFactory.compile("//*[local-name() = 'version']", Filters.element())
		elements = expr.evaluate(document)
		element = elements.getAt(0)
		
		metadata = orchaMetadata.getVersion()
		if(metadata != null){
			element.setText(metadata)
		}
		
		expr = xFactory.compile("//*[local-name() = 'authors']/*[local-name() = 'author']/*[local-name() = 'name']", Filters.element())
		elements = expr.evaluate(document)
		def names = []
		for(Element elt: elements){
			names.add(elt.getValue())
		}

		metadata = orchaMetadata.getAuthor()
		if(metadata != null){
			String author = orchaMetadata.getAuthor()
			if(names.contains(author) == false){
				elements = xFactory.compile("//*[local-name() = 'authors']", Filters.element()).evaluate(document)
				Element authors = elements.get(0)	// authors
				authors.addContent(new Element("author", namespace).addContent(new Element("name", namespace).setText(metadata)))				 
			}	
		}
				 		
		log.info "Requirements updated in XML document"
				
	}

	@Override
	public void updateQualityOfServices(OrchaCodeVisitor orchaCodeVisitor) {
		
		qualityOfService.setQualityOfServiceToInstructions(orchaCodeVisitor)
		
		log.info "Quality of Services updated in XML document"
	}

}
