package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.EventSourcing.JoinPoint

class OutboundChannelAdapter implements Chain, Transformer{
	
	Document xmlSpringIntegration
	
	public OutboundChannelAdapter(Document xmlSpringIntegration) {
		super();
		this.xmlSpringIntegration = xmlSpringIntegration;
	}

	public void file(InstructionNode instructionNode) {
		
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		def instruction = instructionNode.instruction
		
		def outputName = instruction.variable
		EventHandler eventHandler = instruction.springBean
		def outputChannel = outputName + "OutputFileChannelAdapter" + eventHandler.name
		String directoryExpression = "@" + eventHandler.name + ".output.adapter.directory"
		String filenameExpression = "@" + eventHandler.name + ".output.adapter.filename"
					
			if(instruction.variableProperty=="result" || instruction.variableProperty=="error"){
				
				Element chainElement = this.transform(instructionNode, instructionNode.inputName, outputChannel)
				rootElement.addContent(chainElement)
											
			} else if(instruction.variableProperty == "value"){		// case where the previous instruction is like: receive event from file
																	// and the current instruction is like: send event.value to output
			
				// look for the index i of the current instruction into all the inctructions
				int i=0
				while(i<instructions.size() && instructions.get(i)!=instruction){
					i++
				}
				
				// search the previous instruction (decrease i) so its variable matches
				i--
				while(i>=0 && instruction.variable!=instructions.get(i).variable){
					i--
				}
				
				def instructionToConnectToTheInput = instructions.get(i)
				
				Element channelElement = new Element("channel", namespace)
				channelElement.setAttribute("id", instructionToConnectToTheInput.springIntegrationOutputChannel)
				rootElement.addContent(channelElement)
				
				Element chainElement = this.transform(instructionNode, instructionToConnectToTheInput.springIntegrationOutputChannel, outputChannel)
				rootElement.addContent(chainElement)			
			
			} else {
				throw new OrchaCompilationException("Property for variable " + instruction.variable + " should be value or result")
			}
			
			
			Element channelElement = new Element("channel", namespace)
			channelElement.setAttribute("id", outputChannel)
			rootElement.addContent(channelElement)
			
			Element outboundAdapterElement = new Element("outbound-channel-adapter", Namespace.getNamespace("int-file", "http://www.springframework.org/schema/integration/file"))
			outboundAdapterElement.setAttribute("id", "file-"+outputName+eventHandler.name+"Channel-id")
			outboundAdapterElement.setAttribute("channel", outputChannel)
			outboundAdapterElement.setAttribute("directory-expression", directoryExpression)
			outboundAdapterElement.setAttribute("filename-generator-expression", filenameExpression)
			outboundAdapterElement.setAttribute("append-new-line", eventHandler.output.adapter.appendNewLine.toString())
			outboundAdapterElement.setAttribute("mode", eventHandler.output.adapter.writingMode.toString())
			outboundAdapterElement.setAttribute("auto-create-directory", eventHandler.output.adapter.createDirectory.toString())
			outboundAdapterElement.setAttribute("delete-source-files", "false")
			rootElement.addContent(outboundAdapterElement)					
		
	}
	
	private Element transform(InstructionNode instructionNode, String inputChannel, String outputChannel){
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")		
		
		def instruction = instructionNode.instruction
		
		EventHandler eventHandler = instruction.springBean
		
		Element chainElement = chain(inputChannel, outputChannel)
		
		if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){
			if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.before){
					
				Element transformer = new Element("transformer", namespace)
				transformer.setAttribute("expression", "payload")
				chainElement.addContent(transformer)
					
				Element adviceChain = new Element("request-handler-advice-chain", namespace)
				transformer.addContent(adviceChain)
					
				Element refElement = new Element("ref", Namespace.getNamespace("", "http://www.springframework.org/schema/beans"))
				refElement.setAttribute("bean", "eventSourcingAdvice")
				adviceChain.addContent(refElement)
				
			}
		}
			
		if(eventHandler.output.mimeType == "text/plain"){
			Element objectToStringElement = objectToString()
			chainElement.addContent(objectToStringElement)
		} else if(eventHandler.output.mimeType == "application/json"){
			Element objectToJsonElement = objectToJson()
			chainElement.addContent(objectToJsonElement)
		}
		
		return chainElement
	}

}
