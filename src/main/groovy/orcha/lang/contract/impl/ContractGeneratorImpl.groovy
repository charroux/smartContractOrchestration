package orcha.lang.contract.impl

import java.io.File

import javax.xml.XMLConstants
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaMetadata
import orcha.lang.compiler.qualityOfService.CircuitBreakerOption
import orcha.lang.compiler.qualityOfService.EventSourcingOption
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServicesOptions
import orcha.lang.compiler.qualityOfService.QueueOption
import orcha.lang.compiler.qualityOfService.RetryOption
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
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
		
		this.updateProcess(orchaCodeVisitor)
		
		this.updatePrerequisites(orchaCodeVisitor)
		
		this.updateCommitments(orchaCodeVisitor)
		
		this.updateServiceLevelAgreements(orchaCodeVisitor)
		
		this.updateDeliveries(orchaCodeVisitor)
		
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
	public void updatePrerequisites(OrchaCodeVisitor orchaCodeVisitor){
		
		XPathFactory xFactory = XPathFactory.instance()
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'prerequisites']/*[local-name() = 'prerequisite']", Filters.element())
		List<Element> prerequisites = expr.evaluate(document)

		List<InstructionNode> receiveNodes = orchaCodeVisitor.findAllReceiveNodes()
		Element inputEventHandler
		
		receiveNodes.each { receiveNode ->
			
			Element prerequisite = prerequisites.find{ it.getAttribute("eventHandlerName").getValue() == receiveNode.instruction.springBean.name }
			if(prerequisite == null){
				prerequisite = new Element("prerequisite", namespace)
				prerequisite.setAttribute("name", receiveNode.instruction.variable)				
				inputEventHandler = new Element("inputEventHandler", namespace)
				inputEventHandler.setAttribute("name", receiveNode.instruction.springBean.name)
				prerequisite.addContent(inputEventHandler)
				xFactory.compile("//*[local-name() = 'prerequisites']", Filters.element()).evaluate(document).getAt(0).addContent(prerequisite)
			}
			
			this.updateInput(receiveNode, inputEventHandler)
			
		}

		log.info "Prerequisites updated in XML document"
				
	}
	
	@Override
	public void updateDeliveries(OrchaCodeVisitor orchaCodeVisitor){
		
		XPathFactory xFactory = XPathFactory.instance()
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'deliveries']/*[local-name() = 'delivery']", Filters.element())
		List<Element> deliveries = expr.evaluate(document)

		List<InstructionNode> sendNodes = orchaCodeVisitor.findAllSendNodes()
		
		Element outputEventHandler
		
		sendNodes.each { sendNode ->
			
			Element delivery = deliveries.find{ it.getAttribute("eventHandlerName").getValue() == sendNode.instruction.springBean.name }
			if(delivery == null){
				delivery = new Element("delivery", namespace)
				outputEventHandler = new Element("outputEventHandler", namespace)
				outputEventHandler.setAttribute("name", sendNode.instruction.springBean.name)
				delivery.addContent(outputEventHandler)
				xFactory.compile("//*[local-name() = 'deliveries']", Filters.element()).evaluate(document).getAt(0).addContent(delivery)
			}
		
			this.updateOutput(sendNode, outputEventHandler)
		}
		
		log.info "Deliveries updated in XML document"
				
	}
	
	@Override
	public void updateCommitments(OrchaCodeVisitor orchaCodeVisitor){
		
		XPathFactory xFactory = XPathFactory.instance()
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'commitments']/*[local-name() = 'commitment']", Filters.element())
		List<Element> commitments = expr.evaluate(document)

		List<InstructionNode> computeNodes = orchaCodeVisitor.findAllComputeNodes()
		
		computeNodes.each { computeNode ->
			
			Application application = computeNode.instruction.springBean
			
			Element commitment = commitments.find{ it.getAttribute("serviceName").getValue() == application.name }
			if(commitment == null){
				commitment = new Element("commitment", namespace)
				commitment.setAttribute("serviceName", application.name)
				xFactory.compile("//*[local-name() = 'commitments']", Filters.element()).evaluate(document).getAt(0).addContent(commitment)
			}	
			
			if(application.description != null){
				commitment.addContent(new Element("description", namespace).addContent(application.description))
			} 
			if(application.specifications){
				commitment.addContent(new Element("specifications", namespace).addContent(application.specifications))
			}
			
			this.updateInput(computeNode, commitment)
		
			this.updateOutput(computeNode, commitment)
			
		}
		
		log.info "Commitments updated in XML document"
				
	}

	@Override
	public void updateProcess(OrchaCodeVisitor orchaCodeVisitor){
		
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
		
		
		
		
		expr = xFactory.compile("//*[local-name() = 'process']", Filters.element())
		elements = expr.evaluate(document)
		element = elements.getAt(0)
		 
		String orchaSourceProgram = orchaCodeVisitor.getOrchaSourceProgram()
		element.setText(orchaSourceProgram)
		
		 
		 
		 
		
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
				 		
		log.info "Process updated in XML document"
				
	}

	private void updateInput(InstructionNode instructionNode, Element element) {
		
		Object springBean = instructionNode.instruction.springBean	// Application or EventHandler
			 
		if(springBean.input != null){
			Element input = element.getChild("input", namespace)
			Element mimeType
			Element type
			if(input == null){	// there is no input yet
				input = new Element("input", namespace)
				if(springBean.input.mimeType != null){
					mimeType = new Element("mimeType", namespace)
					mimeType.setText(springBean.input.mimeType)
					input.addContent(mimeType)
				}
				if(springBean.input.type != null){
					type = new Element("type", namespace)
					type.setText(springBean.input.type)
					input.addContent(type)
				}
				element.addContent(input)
			} else {	// there is an input
				if(springBean.input.mimeType != null){
					mimeType = input.getChild("mimeType", namespace)
					if(mimeType == null){
						mimeType = new Element("mimeType", namespace)
						input.addContent(mimeType)
					} 						
					mimeType.setText(springBean.input.mimeType)
				}
				if(springBean.input.type != null){
					type = input.getChild("type", namespace)
					if(type == null){
						type = new Element("type", namespace)
						input.addContent(type)
					} 
					type.setText(springBean.input.type)
				}
			}

		}

		log.info "Inputs updated in XML document"
	}
	
	private void updateOutput(InstructionNode instructionNode, Element element) {
		
		Object springBean = instructionNode.instruction.springBean	// Application or EventHandler
			 
		if(springBean.output != null){
		
			Element output = element.getChild("output", namespace)
			Element mimeType
			Element type
			if(output == null){	// there is no output yet
				output = new Element("output", namespace)
				if(springBean.output.mimeType != null){
					mimeType = new Element("mimeType", namespace)
					mimeType.setText(springBean.output.mimeType)
					output.addContent(mimeType)
				}
				if(springBean.output.type != null){
					type = new Element("type", namespace)
					type.setText(springBean.output.type)
					output.addContent(type)
				}
				element.addContent(output)
			} else {	// there is an input
				if(springBean.output.mimeType != null){
					mimeType = output.getChild("mimeType", namespace)
					if(mimeType == null){
						mimeType = new Element("mimeType", namespace)
						output.addContent(mimeType)
					}
					mimeType.setText(springBean.output.mimeType)
				}
				if(springBean.output.type != null){
					type = output.getChild("type", namespace)
					if(type == null){
						type = new Element("type", namespace)
						output.addContent(type)
					}
					type.setText(springBean.output.type)
				}
			}
		}
		
		log.info "Outputs updated in XML document"

	}
	
	private void updateCheckPoint(InstructionNode instructionNode, List<Element> checkpoints){
		
		if(instructionNode.options != null){
			
			EventSourcingOption eventSourcingOption = instructionNode.options.eventSourcing
			
			if(eventSourcingOption != null){
				
				// look for the xml checkpoint that matches the compute node
				Element checkpoint = checkpoints.find{ it.getAttribute("name").getValue() == instructionNode.instruction.springBean.name }
				if(checkpoint == null){
					checkpoint = new Element("checkpoint", namespace)
					checkpoint.setAttribute("name", instructionNode.instruction.springBean.name)
					XPathFactory xFactory = XPathFactory.instance()
					xFactory.compile("//*[local-name() = 'checkpoints']", Filters.element()).evaluate(document).getAt(0).addContent(checkpoint)
				}
			
				if(eventSourcingOption.eventName.empty == false){
					checkpoint.setAttribute("eventName", eventSourcingOption.eventName)
				}		
				checkpoint.setAttribute("position", eventSourcingOption.joinPoint.toString())
			}		
		}
	}
	
	private void updateQualityOfServices(OrchaCodeVisitor orchaCodeVisitor) {
		
		qualityOfService.setQualityOfServiceToInstructions(orchaCodeVisitor)
		
		XPathFactory xFactory = XPathFactory.instance()
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'checkpoints']/*[local-name() = 'checkpoint']", Filters.element())
		List<Element> checkpoints = expr.evaluate(document)

		List<InstructionNode> receiveNodes = orchaCodeVisitor.findAllReceiveNodes()		
		receiveNodes.each { receiveNode ->
			this.updateCheckPoint(receiveNode, checkpoints)
		}

		List<InstructionNode> computeNodes = orchaCodeVisitor.findAllComputeNodes()		
		computeNodes.each { computeNode ->
			this.updateCheckPoint(computeNode, checkpoints)
		}
		
		List<InstructionNode> sendNodes = orchaCodeVisitor.findAllSendNodes()
		sendNodes.each { sendNode ->
			this.updateCheckPoint(sendNode, checkpoints)
		}
		
		
		/*expr = xFactory.compile("//*[local-name() = 'prerequisites']/*[local-name() = 'prerequisite']", Filters.element())
		List<Element> prerequisites = expr.evaluate(document)

		List<InstructionNode> receiveNodes = orchaCodeVisitor.findAllReceiveNodes()
		
		receiveNodes.each { receiveNode ->
			
			if(receiveNode.options != null){
				QualityOfServicesOptions qoSOption = receiveNode.options
				EventSourcingOption eventSourcingOption = qoSOption.eventSourcing
				
				if(eventSourcingOption != null){
					// look for the xml commitment that matches the compute node
					EventHandler eventHandler = receiveNode.instruction.springBean					
					Element prerequisite = prerequisites.find{ it.getAttribute("name").getValue() == eventHandler.name }
					if(commitment == null){
						commitment = new Element("commitment", namespace)
						commitment.addContent(new Element("name", namespace).addContent(eventHandler.name))
						Element commitmentsElement = xFactory.compile("//*[local-name() = 'commitments']", Filters.element()).evaluate(document).get(0)	// commitments
						commitmentsElement.addContent(commitment)
					}
					Element checkPoint = commitment.getChild("checkpoint", namespace)
					Element eventName
					Element joinPoint
					Element messageStore
					if(checkPoint == null){	// there is no checkpoint yet
						checkPoint = new Element("checkpoint", namespace)
						if(eventSourcingOption.eventName != null){
							eventName = new Element("eventName", namespace)
							checkPoint.addContent(eventName)
						}
						joinPoint = new Element("joinpoint", namespace)
						checkPoint.addContent(joinPoint)
						messageStore = new Element("messageStore", namespace)
						checkPoint.addContent(messageStore)
						commitment.addContent(checkPoint)
					} else {
						eventName = checkPoint.getChild("eventName", namespace)
						joinPoint = checkPoint.getChild("joinpoint", namespace)
					}
					if(eventSourcingOption.eventName != null){
						eventName.setText(eventSourcingOption.eventName)
					}
					joinPoint.setText(eventSourcingOption.joinPoint.toString())
				}
			}
		}

		List<InstructionNode> sendNodes = orchaCodeVisitor.findAllSendNodes()
		
		sendNodes.each { sendNode ->
			
			if(sendNode.options != null){
				QualityOfServicesOptions qoSOption = sendNode.options
				EventSourcingOption eventSourcingOption = qoSOption.eventSourcing
				
				if(eventSourcingOption != null){
					// look for the xml commitment that matches the compute node
					EventHandler eventHandler = sendNode.instruction.springBean
					commitments = expr.evaluate(document)			// to avoid dealing with the same event
					Element commitment = commitments.find{ it.getChildText("name", namespace) == eventHandler.name }
					if(commitment == null){
						commitment = new Element("commitment", namespace)
						commitment.addContent(new Element("name", namespace).addContent(eventHandler.name))
						Element commitmentsElement = xFactory.compile("//*[local-name() = 'commitments']", Filters.element()).evaluate(document).get(0)	// commitments
						commitmentsElement.addContent(commitment)
					}
					Element checkPoint = commitment.getChild("checkpoint", namespace)
					Element eventName
					Element joinPoint
					Element messageStore
					if(checkPoint == null){	// there is no checkpoint yet
						checkPoint = new Element("checkpoint", namespace)
						if(eventSourcingOption.eventName != null){
							eventName = new Element("eventName", namespace)
							checkPoint.addContent(eventName)
						}
						joinPoint = new Element("joinpoint", namespace)
						checkPoint.addContent(joinPoint)
						messageStore = new Element("messageStore", namespace)
						checkPoint.addContent(messageStore)
						commitment.addContent(checkPoint)
					} else {
						eventName = checkPoint.getChild("eventName", namespace)
						joinPoint = checkPoint.getChild("joinpoint", namespace)
					}
					if(eventSourcingOption.eventName != null){
						eventName.setText(eventSourcingOption.eventName)
					}
					joinPoint.setText(eventSourcingOption.joinPoint.toString())
				}
			}
		}*/
		
		log.info "Quality of Services updated in XML document"
	}
	
	private void updateSLA(InstructionNode instructionNode, List<Element> serviceLevelAgreements){
		
		XPathFactory xFactory = XPathFactory.instance()
		
		if(instructionNode.options != null){
			
				QualityOfServicesOptions qoSOption = instructionNode.options
				
				RetryOption retryOption = qoSOption.retry
				CircuitBreakerOption circuitBreakerOption = qoSOption.circuitBreaker
				QueueOption queueOption = qoSOption.queue
				
				if(retryOption!=null || circuitBreakerOption!=null || queueOption!=null){
					Element performanceLevel
					Element retryPattern
					Element circuitBreakerPattern
					Element messageQueue
					Element agreement = serviceLevelAgreements.find{ it.getAttribute("name").getValue() == instructionNode.instruction.springBean.name }
					if(agreement == null){
						agreement = new Element("serviceLevelAgreement", namespace)
						agreement.setAttribute("name", instructionNode.instruction.springBean.name)
						xFactory.compile("//*[local-name() = 'serviceLevelAgreements']", Filters.element()).evaluate(document).getAt(0).addContent(agreement)
						performanceLevel = new Element("performanceLevel", namespace)
						agreement.addContent(performanceLevel)
						if(retryOption != null){
							retryPattern = new Element("retryPattern", namespace)
							performanceLevel.addContent(retryPattern)
						}
						if(circuitBreakerOption != null){
							circuitBreakerPattern = new Element("circuitBreakerPattern", namespace)
							performanceLevel.addContent(circuitBreakerPattern)
						}
						if(queueOption != null){
							messageQueue = new Element("messageQueue", namespace)
							performanceLevel.addContent(messageQueue)
						}
					} else {
						performanceLevel = agreement.getChild("performanceLevel", namespace)
						if(performanceLevel == null){
							performanceLevel = new Element("performanceLevel", namespace)
							agreement.addContent(performanceLevel)
						}
						if(retryOption != null){
							retryPattern = performanceLevel.getChild("retryPattern", namespace)
							if(retryPattern == null){
								retryPattern = new Element("retryPattern", namespace)
								performanceLevel.addContent(retryPattern)
							}
						}
						if(circuitBreakerOption != null){
							circuitBreakerPattern = performanceLevel.getChild("circuitBreakerPattern", namespace)
							if(circuitBreakerPattern == null){
								circuitBreakerPattern = new Element("circuitBreakerPattern", namespace)
								performanceLevel.addContent(circuitBreakerPattern)
							}
						}
						if(queueOption != null){
							messageQueue = performanceLevel.getChild("messageQueue", namespace)
							if(messageQueue == null){
								messageQueue = new Element("messageQueue", namespace)
								performanceLevel.addContent(messageQueue)
							}
						}

					}
					
					if(retryOption != null){
						retryPattern.setAttribute("maxNumberOfAttempts", retryOption.getMaxNumberOfAttempts().toString())
						retryPattern.setAttribute("intervalBetweenTheFirstAndSecondAttempt", retryOption.getIntervalBetweenTheFirstAndSecondAttempt().toString())
						retryPattern.setAttribute("intervalMultiplierBetweenAttemps", retryOption.getIntervalMultiplierBetweenAttemps().toString())
						retryPattern.setAttribute("maximumIntervalBetweenAttempts", retryOption.getMaximumIntervalBetweenAttempts().toString())
					}

					if(circuitBreakerOption != null){
						circuitBreakerPattern.setAttribute("numberOfFailuresBeforeOpening", circuitBreakerOption.getNumberOfFailuresBeforeOpening().toString())
						circuitBreakerPattern.setAttribute("intervalBeforeHalfOpening", circuitBreakerOption.getIntervalBeforeHalfOpening().toString())
					}
					
					if(queueOption != null){
						
						messageQueue.setAttribute("capacity", queueOption.getCapacity().toString())
						
						long fixedDelay = queueOption.getFixedDelay()
						if(fixedDelay != -1L){
							messageQueue.setAttribute("fixedDelay", fixedDelay.toString())
						}
						
						long fixedRate = queueOption.getFixedRate()
						if(fixedRate != -1L){
							messageQueue.setAttribute("fixedRate", fixedRate.toString())
						}
						
						String cron = queueOption.getCron()
						if(cron != ""){
							messageQueue.setAttribute("cron", cron)
						}
					}

				}
			}
	}
	
	@Override
	public void updateServiceLevelAgreements(OrchaCodeVisitor orchaCodeVisitor){

		qualityOfService.setQualityOfServiceToInstructions(orchaCodeVisitor)
		
		XPathFactory xFactory = XPathFactory.instance()
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'serviceLevelAgreements']/*[local-name() = 'serviceLevelAgreement']", Filters.element())
		List<Element> serviceLevelAgreements = expr.evaluate(document)

		List<InstructionNode> receiveNodes = orchaCodeVisitor.findAllReceiveNodes()
		receiveNodes.each { receiveNode ->
			
			this.updateSLA(receiveNode, serviceLevelAgreements)
			
		}

		List<InstructionNode> computeNodes = orchaCodeVisitor.findAllComputeNodes()		
		computeNodes.each { computeNode ->
			
			this.updateSLA(computeNode, serviceLevelAgreements)
			
		}

		List<InstructionNode> sendNodes = orchaCodeVisitor.findAllSendNodes()
		sendNodes.each { sendNode ->
			
			this.updateSLA(sendNode, serviceLevelAgreements)
			
		}

/*			if(computeNode.options != null){
			
				QualityOfServicesOptions qoSOption = computeNode.options
				
				RetryOption retryOption = qoSOption.retry
				CircuitBreakerOption circuitBreakerOption = qoSOption.circuitBreaker
				QueueOption queueOption = qoSOption.queue
				
				if(retryOption!=null || circuitBreakerOption!=null || queueOption!=null){
					Element performanceLevel
					Element retryPattern
					Element circuitBreakerPattern
					Element messageQueue
					Element agreement = serviceLevelAgreements.find{ it.getAttribute("name").getValue() == computeNode.instruction.springBean.name }
					if(agreement == null){
						agreement = new Element("serviceLevelAgreement", namespace)
						agreement.setAttribute("name", computeNode.instruction.springBean.name)
						xFactory.compile("//*[local-name() = 'serviceLevelAgreements']", Filters.element()).evaluate(document).getAt(0).addContent(agreement)												
						performanceLevel = new Element("performanceLevel", namespace)
						agreement.addContent(performanceLevel)
						if(retryOption != null){
							retryPattern = new Element("retryPattern", namespace)
							performanceLevel.addContent(retryPattern)
						}
						if(circuitBreakerOption != null){
							circuitBreakerPattern = new Element("circuitBreakerPattern", namespace)
							performanceLevel.addContent(circuitBreakerPattern)
						}	
						if(queueOption != null){
							messageQueue = new Element("messageQueue", namespace)
							performanceLevel.addContent(messageQueue)
						}
					} else {
						performanceLevel = agreement.getChild("performanceLevel", namespace)
						if(performanceLevel == null){
							performanceLevel = new Element("performanceLevel", namespace)
							agreement.addContent(performanceLevel)
						} 
						if(retryOption != null){
							retryPattern = performanceLevel.getChild("retryPattern", namespace)
							if(retryPattern == null){
								retryPattern = new Element("retryPattern", namespace)
								performanceLevel.addContent(retryPattern)
							}
						}
						if(circuitBreakerOption != null){
							circuitBreakerPattern = performanceLevel.getChild("circuitBreakerPattern", namespace)
							if(circuitBreakerPattern == null){
								circuitBreakerPattern = new Element("circuitBreakerPattern", namespace)
								performanceLevel.addContent(circuitBreakerPattern)
							}
						}
						if(queueOption != null){
							messageQueue = performanceLevel.getChild("messageQueue", namespace)
							if(messageQueue == null){
								messageQueue = new Element("messageQueue", namespace)
								performanceLevel.addContent(messageQueue)
							}							
						}

					}
					
					if(retryOption != null){
						retryPattern.setAttribute("maxNumberOfAttempts", retryOption.getMaxNumberOfAttempts().toString())
						retryPattern.setAttribute("intervalBetweenTheFirstAndSecondAttempt", retryOption.getIntervalBetweenTheFirstAndSecondAttempt().toString())
						retryPattern.setAttribute("intervalMultiplierBetweenAttemps", retryOption.getIntervalMultiplierBetweenAttemps().toString())
						retryPattern.setAttribute("maximumIntervalBetweenAttempts", retryOption.getMaximumIntervalBetweenAttempts().toString())	
					}

					if(circuitBreakerOption != null){
						circuitBreakerPattern.setAttribute("numberOfFailuresBeforeOpening", circuitBreakerOption.getNumberOfFailuresBeforeOpening().toString())
						circuitBreakerPattern.setAttribute("intervalBeforeHalfOpening", circuitBreakerOption.getIntervalBeforeHalfOpening().toString())
					}
					
					if(queueOption != null){
						
						messageQueue.setAttribute("capacity", queueOption.getCapacity().toString())
						
						long fixedDelay = queueOption.getFixedDelay()
						if(fixedDelay != -1L){
							messageQueue.setAttribute("fixedDelay", fixedDelay.toString())
						}
						
						long fixedRate = queueOption.getFixedRate()
						if(fixedRate != -1L){
							messageQueue.setAttribute("fixedRate", fixedRate.toString())
						}
						
						String cron = queueOption.getCron()
						if(cron != ""){
							messageQueue.setAttribute("cron", cron)
						}
					}

				}
			}
		}*/

		qualityOfService.setQualityOfServiceToInstructions(orchaCodeVisitor)
		
		log.info "Service-level Agreements updated in XML document"
	}


}
