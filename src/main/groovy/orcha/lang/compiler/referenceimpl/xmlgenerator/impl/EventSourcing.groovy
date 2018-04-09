package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import java.util.List

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.qualityOfService.EventSourcingOption

class EventSourcing implements Bean, HeaderEnricher, QoS{
	
	Document xmlSpringIntegration
	
	public EventSourcing(Document xmlSpringIntegration) {
		super();
		this.xmlSpringIntegration = xmlSpringIntegration;
	}
	
	private mongoDBEventSourcing() {
	
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element beanElement = bean("eventSourcingMongoDBAdvice", "orcha.lang.compiler.referenceimpl.xmlgenerator.impl.EventSourcingMongoDBAdvice")
		rootElement.addContent(beanElement)
		
		Element channelElement = new Element("channel", namespace)
		channelElement.setAttribute("id", "eventSourcingMongoDBChannel")
		rootElement.addContent(channelElement)
		
		Element headerEnricherElement = headerEnricher("eventSourcingMongoDBChannel", "eventSourcingMongoDBQueueChannel", "timestampSession", "@orchaSession.timestamp")
		rootElement.addContent(headerEnricherElement)
		
		Element messageStoreQueueElement = messageStoreQueue("eventSourcingMongoDBQueueChannel", "mongoDbMessageStore")
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

	private redisEventSourcing() {
		
		Element rootElement = xmlSpringIntegration.getRootElement()
			
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
			
		Element beanElement = bean("eventSourcingRedisAdvice", "orcha.lang.compiler.referenceimpl.xmlgenerator.impl.EventSourcingRedisAdvice")
		rootElement.addContent(beanElement)
			
		Element channelElement = new Element("channel", namespace)
		channelElement.setAttribute("id", "eventSourcingRedisChannel")
		rootElement.addContent(channelElement)
			
		Element headerEnricherElement = headerEnricher("eventSourcingRedisChannel", "eventSourcingRedisQueueChannel", "timestampSession", "@orchaSession.timestamp")
		rootElement.addContent(headerEnricherElement)
			
		Element messageStoreQueueElement = messageStoreQueue("eventSourcingRedisQueueChannel", "redisMessageStore")
		rootElement.addContent(messageStoreQueueElement)
		
		beanElement = bean("redisMessageStore", "org.springframework.integration.redis.store.RedisMessageStore")
		Element constructorArgElement = new Element("constructor-arg", Namespace.getNamespace("", "http://www.springframework.org/schema/beans"))
		constructorArgElement.setAttribute("ref", "redisConnectionFactory")
		beanElement.addContent(constructorArgElement)
		rootElement.addContent(beanElement)
		
		def properties = [port: '6379']
		beanElement = beanWithValue("redisConnectionFactory", "org.springframework.data.redis.connection.jedis.JedisConnectionFactory", properties)
		rootElement.addContent(beanElement)
		
	}
	
	public void eventSourcing(List<EventSourcingOption> eventsSourcing){
		
		def founded = []
		
		eventsSourcing.each{ eventSourcingOption ->
			
			if(founded.contains(eventSourcingOption.messageStore) == false) {
			
				switch(eventSourcingOption.messageStore) {
					
					case orcha.lang.configuration.EventSourcing.MessageStore.mongoDB:
						mongoDBEventSourcing()
						founded.add(eventSourcingOption.messageStore)
						break;
						
					case orcha.lang.configuration.EventSourcing.MessageStore.Redis:
						founded.add(eventSourcingOption.messageStore)
						redisEventSourcing()
						break;
						
					default:
						throw new OrchaCompilationException(eventSourcingOption.messageStore.toString() + " not supported yet.")
				}
			
			}
			
				
		}
		
	}		
		
}
