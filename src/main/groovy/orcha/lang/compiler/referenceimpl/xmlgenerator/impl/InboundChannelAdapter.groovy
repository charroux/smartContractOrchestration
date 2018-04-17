package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.qualityOfService.QueueOption
import orcha.lang.configuration.HttpAdapter
import orcha.lang.configuration.Input
import org.springframework.http.MediaType

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.springframework.beans.factory.annotation.Autowired

class InboundChannelAdapter implements Poller, Chain, HeaderEnricher, Filter, Transformer {
	
	Document xmlSpringIntegration
	
	public InboundChannelAdapter(Document xmlSpringIntegration) {
		super();
		this.xmlSpringIntegration = xmlSpringIntegration;
	}
	
	private Element addDefaultPoller(InstructionNode instructionNode) {

		Element pollerElement

			QueueOption queueOption = new QueueOption(fixedDelay: 1000)
			pollerElement = poller(queueOption)
			pollerElement.setAttribute("default", "true")
	
		return pollerElement
	}
	
	public void file(InstructionNode instructionNode) {

		Element rootElement = xmlSpringIntegration.getRootElement()	

		Element pollerElement = this.addDefaultPoller(instructionNode)
		rootElement.addContent(pollerElement)

		Instruction instruction = instructionNode.instruction
		def inputChannel = instructionNode.inputName
		
		Namespace namespace = Namespace.getNamespace("int-file", "http://www.springframework.org/schema/integration/file")
		
		Element fileAdapter = new Element("inbound-channel-adapter", namespace)
		fileAdapter.setAttribute("id", "file-"+inputChannel+"-id")
		fileAdapter.setAttribute("directory", instruction.springBean.input.adapter.directory)
		fileAdapter.setAttribute("channel", inputChannel)
		fileAdapter.setAttribute("prevent-duplicates", "true")
		
		if(instruction.springBean.input.adapter.filenamePattern != null){
			fileAdapter.setAttribute("filename-pattern", instruction.springBean.input.adapter.filenamePattern)
		}
		
		if(instructionNode.options!=null && instructionNode.options.queue!=null){
			fileAdapter.setAttribute("queue-size", instructionNode.options.queue.capacity.toString())
		}
		
		rootElement.addContent(fileAdapter)
		
		Element stringTransformer = new Element("file-to-string-transformer", namespace)
		stringTransformer.setAttribute("input-channel", inputChannel)
		stringTransformer.setAttribute("output-channel", inputChannel+"Transformer")
		stringTransformer.setAttribute("delete-files", "false")
		rootElement.addContent(stringTransformer)
			
		String[] typeAndSubtype = instruction.springBean.input.mimeType.split("/");

		if(typeAndSubtype.length != 2){
			throw new OrchaCompilationException("Unknown Mime Type:" + instruction.springBean.input.mimeType)
		}
			
		Element chain = chain(inputChannel+"Transformer", instructionNode.outputName)
		rootElement.addContent(chain)
			
		MediaType mediaType = new  MediaType(typeAndSubtype[0], typeAndSubtype[1])
			
		if(mediaType.equals(MediaType.APPLICATION_JSON)){								
			Element jsonToObjectTransformer = jsonToObject(instruction.springBean.input.type) 
			chain.addContent(jsonToObjectTransformer)				
		}
				
		def id = "headers['id'].toString()"
		Element messageIDEnricher = headerEnricher("messageID", id)
		chain.addContent(messageIDEnricher)
			
		if(instructionNode.next.instruction.instruction!="receive" && instructionNode.instruction.condition!=null){
			String condition = instructionNode.instruction.condition
			
			condition = condition.trim()
			
			if(condition.startsWith("==")) {
				condition = "payload " + condition
			} else {
				condition = "payload." + condition
			}
			
			
			//condition = condition.replaceFirst(instructionNode.instruction.variable, "payload")
			
			Element conditionFilter = filter(condition)
			chain.addContent(conditionFilter)
		}
				
	}
	
	public void http(InstructionNode instructionNode) {

		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Instruction instruction = instructionNode.instruction
		def inputChannel = instructionNode.inputName
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
			
		Element channel = new Element("channel", namespace)
		channel.setAttribute("id", inputChannel)
		rootElement.addContent(channel)
		
		namespace = Namespace.getNamespace("int-http", "http://www.springframework.org/schema/integration/http")
		
		HttpAdapter httpAdapter = instruction.springBean.input.adapter
		
		Element adapter = new Element("inbound-channel-adapter", namespace)
		adapter.setAttribute("id", "http-"+inputChannel+"-id")
		adapter.setAttribute("channel", inputChannel)
		adapter.setAttribute("status-code-expression", "T(org.springframework.http.HttpStatus).NO_CONTENT")
		adapter.setAttribute("supported-methods", httpAdapter.method.toString())
		adapter.setAttribute("path", httpAdapter.url)
		
		Input input = instruction.springBean.input		
		adapter.setAttribute("request-payload-type", input.type)
		
		Element requestMapping = new Element("request-mapping", namespace)		
		requestMapping.setAttribute("consumes", input.mimeType)
		adapter.addContent(requestMapping)
		
		rootElement.addContent(adapter)
		
		Element chain = chain(inputChannel, instructionNode.outputName)
		rootElement.addContent(chain)
		
		def id = "headers['id'].toString()"
		Element messageIDEnricher = headerEnricher("messageID", id)
		chain.addContent(messageIDEnricher)
		
	}
	
}
