package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.qualityOfService.QueueOption
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

	private Element inboundChannelAdapter(InstructionNode instructionNode){
		
		Instruction instruction = instructionNode.instruction
		def inputChannel = instructionNode.inputName
		
		Namespace namespace = Namespace.getNamespace("int-file", "http://www.springframework.org/schema/integration/file")
		
		Element element = new Element("inbound-channel-adapter", namespace)
		element.setAttribute("id", "file-"+inputChannel+"-id")
		element.setAttribute("directory", instruction.springBean.input.adapter.directory)
		element.setAttribute("channel", inputChannel)
		element.setAttribute("prevent-duplicates", "true")
		
		if(instruction.springBean.input.adapter.filenamePattern != null){
			element.setAttribute("filename-pattern", instruction.springBean.input.adapter.filenamePattern)
		}		
		
		Element pollerElement
		
		if(instructionNode.options!=null && instructionNode.options.queue!=null){
			element.setAttribute("queue-size", instructionNode.options.queue.capacity)
			pollerElement = poller(instructionNode.options.queue)
		} else {
			QueueOption queueOption = new QueueOption(fixedDelay: 1000)
			pollerElement = poller(queueOption)
		}
		
		element.addContent(pollerElement)
				
		return element
	}
	
	public void file(InstructionNode instructionNode) {

		Element rootElement = xmlSpringIntegration.getRootElement()					
		
		Element fileAdapter = this.inboundChannelAdapter(instructionNode)
		rootElement.addContent(fileAdapter)
		
		Namespace namespace = Namespace.getNamespace("int-file", "http://www.springframework.org/schema/integration/file")
		
		Instruction instruction = instructionNode.instruction
		
		def inputChannel = instructionNode.inputName
		
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
			condition = condition.replaceFirst(instructionNode.instruction.variable, "payload")
			Element conditionFilter = filter(condition)
			chain.addContent(conditionFilter)
		}
				
	}
	
}
