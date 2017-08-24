package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import orcha.lang.compiler.qualityOfService.EventSourcingOption
import orcha.lang.compiler.qualityOfService.QueueOption
import orcha.lang.compiler.qualityOfService.RetryOption
import org.jdom2.Element
import org.jdom2.Namespace

trait QoS {
	
	public Element retry(RetryOption retryOption){
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element retry = new Element("retry-advice", namespace)		
		retry.setAttribute("max-attempts", retryOption.maxNumberOfAttempts.toString())
		retry.setAttribute("recovery-channel", "recoveryChannel")
		
		Element backOff = new Element("exponential-back-off", namespace)
		backOff.setAttribute("initial", retryOption.intervalBetweenTheFirstAndSecondAttempt.toString())
		backOff.setAttribute("multiplier", retryOption.intervalMultiplierBetweenAttemps.toString())
		backOff.setAttribute("maximum", retryOption.maximumIntervalBetweenAttempts.toString())
		
		retry.addContent(backOff)
		
		return retry
		
	}
	
	public Element eventSourcing(EventSourcingOption eventSourcingOption){
		
		Namespace namespace = Namespace.getNamespace("", "http://www.springframework.org/schema/beans")

		Element eventSourcing = new Element("ref", namespace)
		eventSourcing.setAttribute("bean", "eventSourcingAdvice")
		
		return eventSourcing
		
	}
	
	public Element queue(String queueName, QueueOption queueOption){
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element channel = new Element("channel", namespace)
		channel.setAttribute("id", queueName)
		
		Element queue = new Element("queue", namespace)
		queue.setAttribute("capacity", queueOption.capacity.toString())
		channel.addContent(queue)
		
		return channel

	}
	
	public Element messageStoreQueue(String queueName, String messageStore){
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element channel = new Element("channel", namespace)
		channel.setAttribute("id", queueName)
		
		Element queue = new Element("queue", namespace)
		queue.setAttribute("message-store", messageStore)
		channel.addContent(queue)
		
		return channel

	}

}
