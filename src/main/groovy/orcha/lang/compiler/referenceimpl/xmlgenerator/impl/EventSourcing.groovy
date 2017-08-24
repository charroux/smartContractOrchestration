package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import java.util.List

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import orcha.lang.compiler.InstructionNode

class EventSourcing implements Bean, HeaderEnricher, QoS{
	
	Document xmlSpringIntegration
	
	public EventSourcing(Document xmlSpringIntegration) {
		super();
		this.xmlSpringIntegration = xmlSpringIntegration;
	}
	
	public void eventSourcing(List<InstructionNode> eventsSourcing){
		
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element beanElement = bean("eventSourcingAdvice", "orcha.lang.compiler.referenceimpl.xmlgenerator.impl.EventSourcingAdvice")
		rootElement.addContent(beanElement)
		
		Element channelElement = new Element("channel", namespace)
		channelElement.setAttribute("id", "eventSourcingChannel")
		rootElement.addContent(channelElement)
		
		Element headerEnricherElement = headerEnricher("eventSourcingChannel", "eventSourcingQueueChannel", "timestampSession", "@orchaSession.timestamp")
		rootElement.addContent(headerEnricherElement)
		
		Element messageStoreQueueElement = messageStoreQueue("eventSourcingQueueChannel", "mongoDbMessageStore")
		rootElement.addContent(messageStoreQueueElement)
		
		beanElement = bean("mongoDbMessageStore", "org.springframework.integration.mongodb.store.MongoDbMessageStore")
		Element constructorArgElement = new Element("constructor-arg", Namespace.getNamespace("", "http://www.springframework.org/schema/beans"))
		constructorArgElement.setAttribute("ref", "mongoDbFactory")
		beanElement.addContent(constructorArgElement)
		rootElement.addContent(beanElement)
		
		beanElement = bean("mongoDbFactory", "org.springframework.data.mongodb.core.SimpleMongoDbFactory")
		constructorArgElement = new Element("constructor-arg", Namespace.getNamespace("", "http://www.springframework.org/schema/beans"))
		beanElement.addContent(constructorArgElement)
		Element bean = bean("com.mongodb.Mongo")
		constructorArgElement.addContent(bean)
		constructorArgElement = new Element("constructor-arg", Namespace.getNamespace("", "http://www.springframework.org/schema/beans"))
		constructorArgElement.setAttribute("value", "orchaEventSourcing")
		beanElement.addContent(constructorArgElement)
		rootElement.addContent(beanElement)
	
	}

}
