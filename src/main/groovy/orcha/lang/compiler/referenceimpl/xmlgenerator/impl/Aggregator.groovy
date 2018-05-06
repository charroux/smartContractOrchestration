package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.qualityOfService.QualityOfServicesOptions
import orcha.lang.compiler.qualityOfService.QueueOption
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace

class Aggregator implements Transformer, QoS{

	Document xmlSpringIntegration
	
	public Aggregator(Document xmlSpringIntegration) {
		super();
		this.xmlSpringIntegration = xmlSpringIntegration;
	}
	
	public void aggregate(InstructionNode instructionNode, String releaseExpression, List<String> applicationsNames, String transformerExpression, boolean isMultipleArgumentsInExpression) {
		
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		
		boolean sameEvent = instructionNode.options.sameEvent
		
		// in the case of when "(codeToBenchmark1 terminates condition == -1) and (codeToBenchmark2 terminates condition == 1)"
		// codeToBenchmark1 should be received first, then codeToBenchmark2. So a resequencer of messages is needed
		
		int sequenceSize = applicationsNames.size()
		
		if(sequenceSize > 1) {

			Element resequencerElement = new Element("resequencer", namespace)
			resequencerElement.setAttribute("id", "resequencer-"+instructionNode.inputName+"-id")
			resequencerElement.setAttribute("input-channel", instructionNode.inputName)
			resequencerElement.setAttribute("output-channel", instructionNode.inputName + "Resequencer")
			resequencerElement.setAttribute("release-partial-sequences", "false")
			resequencerElement.setAttribute("release-strategy-expression", "size()==" + sequenceSize)
			
			if(sameEvent == true){
				
				resequencerElement.setAttribute("correlation-strategy-expression", "headers['messageID']")
						
			} else {
			
				resequencerElement.setAttribute("correlation-strategy-expression", "0")
											
			}
			
			rootElement.addContent(resequencerElement)
	
		}
		
		Instruction instruction = instructionNode.instruction
					 
		def outputChannel = instructionNode.inputName + "AggregatorOutput"
			
		if(instructionNode.options == null){
			instructionNode.options = new QualityOfServicesOptions(sameEvent: false)
		}
			
		Element aggregatorElement = new Element("aggregator", namespace)
		aggregatorElement.setAttribute("id", "aggregator-"+instructionNode.inputName+"-id")
		
		if(sequenceSize > 1) {
			aggregatorElement.setAttribute("input-channel", instructionNode.inputName + "Resequencer")
		} else {
			aggregatorElement.setAttribute("input-channel", instructionNode.inputName)
		}
		
		aggregatorElement.setAttribute("output-channel", instructionNode.inputName + "Transformer")
		aggregatorElement.setAttribute("release-strategy-expression", releaseExpression)
		
		if(sameEvent == true){
					
			aggregatorElement.setAttribute("correlation-strategy-expression", "headers['messageID']")
					
		} else {

			aggregatorElement.setAttribute("correlation-strategy-expression", "0")
										
		}
				
		rootElement.addContent(aggregatorElement)
		
		Element transformer = new Element("transformer", namespace)
		transformer.setAttribute("id", "transformer-"+instructionNode.inputName+"-id")
		transformer.setAttribute("input-channel", instructionNode.inputName + "Transformer")
		transformer.setAttribute("output-channel", outputChannel)
		transformer.setAttribute("expression", transformerExpression)
										
		rootElement.addContent(transformer)
		
		if(isMultipleArgumentsInExpression == true){
					
			Element applicationsListToObjectsListElement = applicationsListToObjectsListTransformer(instructionNode)
			rootElement.addContent(applicationsListToObjectsListElement)
			
		} else {
					
			Element applicationToObjectElement = applicationToObjectTransformer(instructionNode)
			rootElement.addContent(applicationToObjectElement)
			
		}
				
		if(instructionNode.options!=null && instructionNode.options.queue!=null){
					
			Element queueElement = queue(instructionNode.outputName, instructionNode.options.queue)
			rootElement.addContent(queueElement)
			
		}
			
		
	}
}
