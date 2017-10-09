package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.EventSourcing.JoinPoint

class ServiceActivator implements Chain, Poller, QoS, orcha.lang.compiler.referenceimpl.xmlgenerator.impl.Bean, Transformer{
	
	Document xmlSpringIntegration
	public ServiceActivator(Document xmlSpringIntegration) {
		super();
		this.xmlSpringIntegration = xmlSpringIntegration;
	}

	public void service(InstructionNode instructionNode, boolean computeFails, String failChannel, boolean isScript){
		
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Instruction instruction = instructionNode.instruction
		
		def applicationName = instruction.springBean.name
		String outputServiceChannel = applicationName + "ServiceAcivatorOutput"
		String id = applicationName + 'Channel'
/*		def transformerBean = applicationName + "TransformerBean"
		def transformerRef = applicationName
		
		def outputTransformerChannel = outputServiceChannel + "TransformerChannel"
		
		def errorExpression = '@errorUnwrapper.transform(payload)'*/
		
		Element chain = chain("service-activator-chain-"+id+"-id", instructionNode.inputName, outputServiceChannel)
		rootElement.addContent(chain)
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		if(computeFails == true){
			
			
			Element headerEnricher = new Element("header-enricher", namespace)
			chain.addContent(headerEnricher)
			
			Element header = new Element("error-channel", namespace)
			headerEnricher.addContent(header)
			
			header.setAttribute("ref", failChannel+"-id")
			
		}
		
		
		Element serviceActivatorElement
				
		if(isScript == false){

			JavaServiceAdapter javaServiceAdapter = (JavaServiceAdapter)instruction.springBean.input.adapter
			String className = javaServiceAdapter.javaClass
			className = className.substring(className.lastIndexOf('.')+1)
			className = className.substring(0,1).toLowerCase() + className.substring(1)
			def methodName = instruction.springBean.input.adapter.method
			def expression = '@' + className + '.' + methodName + '(payload)'
			
			serviceActivatorElement = new Element("service-activator", namespace)
			serviceActivatorElement.setAttribute("id", "service-activator-"+id+"-id")
			serviceActivatorElement.setAttribute("expression", expression)
			
		} else {

			ScriptServiceAdapter scriptingServiceAdapter = (ScriptServiceAdapter)instructionNode.instruction.springBean.input.adapter
			def location = scriptingServiceAdapter.file
			def language = instructionNode.instruction.springBean.language

			serviceActivatorElement = new Element("service-activator", namespace)
			serviceActivatorElement.setAttribute("id", "service-activator-"+id+"-id")
										
			Element scriptElement = new Element("script", Namespace.getNamespace("int-script", "http://www.springframework.org/schema/integration/scripting"))
			scriptElement.setAttribute("lang", language)
			scriptElement.setAttribute("location", location)
					
			serviceActivatorElement.addContent(scriptElement)

		}
				
		chain.addContent(serviceActivatorElement)
											
		if(instructionNode.options!=null && instructionNode.options.queue!=null){
			
			Element pollerElement = poller(instructionNode.options.queue)
			serviceActivatorElement.addContent(pollerElement)
							
		}
						
		Element adviceChainElement = new Element("request-handler-advice-chain", namespace)
		serviceActivatorElement.addContent(adviceChainElement)
		
		if(instructionNode.options!=null && instructionNode.options.retry!=null && instructionNode.options.circuitBreaker!=null && (instructionNode.options.retry.orderInChain < instructionNode.options.circuitBreaker.orderInChain)){
										
			Element retryElement = retry(instructionNode.options.retry)
			adviceChainElement.addContent(retryElement)
										
			def properties = [threshold: instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening.toString(), halfOpenAfter: instructionNode.options.circuitBreaker.intervalBeforeHalfOpening.toString() ]
			Element beanElement = beanWithValue("org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice", properties)
			adviceChainElement.addContent(beanElement)
									
		} 
							
		if(instructionNode.options!=null && instructionNode.options.retry!=null && instructionNode.options.circuitBreaker!=null && (instructionNode.options.retry.orderInChain >= instructionNode.options.circuitBreaker.orderInChain)){
										
			def properties = [threshold: instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening.toString(), halfOpenAfter: instructionNode.options.circuitBreaker.intervalBeforeHalfOpening.toString() ]
			Element beanElement = beanWithValue("org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice", properties)
			adviceChainElement.addContent(beanElement)
										
			Element retryElement = retry(instructionNode.options.retry)
			adviceChainElement.addContent(retryElement)
		}
							
		if(instructionNode.options!=null && instructionNode.options.retry!=null && instructionNode.options.circuitBreaker==null){
						
			Element retryElement = retry(instructionNode.options.retry)
			adviceChainElement.addContent(retryElement)
						
		}
							
		if(instructionNode.options!=null && instructionNode.options.retry==null && instructionNode.options.circuitBreaker!=null){
								
			def properties = [threshold: instructionNode.options.circuitBreaker.numberOfFailuresBeforeOpening.toString(), halfOpenAfter: instructionNode.options.circuitBreaker.intervalBeforeHalfOpening.toString() ]
			Element beanElement = beanWithValue("org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice", properties)
			adviceChainElement.addContent(beanElement)
								
		}
																						
		if(instructionNode.options.eventSourcing!=null){
						
			if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.before || instructionNode.options.eventSourcing.joinPoint==JoinPoint.beforeAndAfter){
				Element eventSourcingElement = eventSourcing(instructionNode.options.eventSourcing)
				adviceChainElement.addContent(eventSourcingElement)						
			}															
								
		}
		
		Element objectToApplicationTransformerElement = objectToApplicationTransformer(instructionNode)
		rootElement.addContent(objectToApplicationTransformerElement)
						
	}

}
