package orcha.lang.contract.impl

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
import org.jdom2.xpath.XPathExpression
import org.jdom2.xpath.XPathFactory
import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaderJDOMFactory
import org.jdom2.input.sax.XMLReaderSchemaFactory
import org.jdom2.output.XMLOutputter
import org.jdom2.output.support.XMLOutputProcessor
import org.jdom2.output.Format

class ContractGeneratorImpl implements ContractGenerator{

	Document document
	String xmlContract
	
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
		document = sb.build(new File(xmlContract));
	}
	
	@Override
	public String generate(OrchaCodeVisitor orchaCodeVisitor) {
		
		List<InstructionNode> computeNodes = orchaCodeVisitor.findAllComputeNodes()
		
		updateCommitment(computeNodes)
		
		return xmlContract
		
	}
	
	private void updateCommitment(def computeNodes){
		
		XPathFactory xFactory = XPathFactory.instance()
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'commitments']/*[local-name() = 'commitment']/*[local-name() = 'name']", Filters.element())
		List<Element> elements = expr.evaluate(document)
		def names = []
		for(Element element: elements){
			names.add(element.getValue())
		}

		computeNodes.each { computeNode ->
			Application application = (Application)computeNode.instruction.springBean
			if(names.contains(application.name) == false){
				elements = xFactory.compile("//*[local-name() = 'commitments']", Filters.element()).evaluate(document)
				Element commitments = elements.get(0)	// commitments
				Element element = new Element("commitment")
				println application.name + ' ' + application.description + ' ' + application.specifications
				if(application.name != null){
					element.addContent(new Element("name").addContent(application.name))
				}
				if(application.description != null){
					element.addContent(new Element("description").addContent(application.description))
				} 
				if(application.specifications){
					element.addContent(new Element("specifications").addContent(application.specifications))
				}
				commitments.addContent(element)
			}
		}
		
		/*XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'commitments']", Filters.element())
		 List<Element> elements = expr.evaluate(document)
		 if(elements.size() == 1){
			 Element commitments = elements.get(0)	// commitments
			 Element element = new Element("commitment")
			 element.addContent(new Element("name").addContent("essai"))
			 commitments.addContent(element)
		 }*/
		 
		 XMLOutputter xml = new XMLOutputter();
		 xml.setFormat(Format.getPrettyFormat());
		 System.out.println(xml.outputString(document));
		 XMLOutputProcessor xmlProcessor = xml.getXMLOutputProcessor()
		 String outputFile = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "contract1.xml"
		 FileWriter fw = new FileWriter(new File(outputFile));
		 xmlProcessor.process(fw, Format.getPrettyFormat(), document)
	}

}
