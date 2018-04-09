package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.handler.advice.AbstractRequestHandlerAdvice
import org.springframework.integration.handler.advice.AbstractRequestHandlerAdvice.ExecutionCallback
import org.springframework.messaging.Message

import groovy.util.logging.Slf4j

@Slf4j
class EventSourcingRedisAdvice extends AbstractRequestHandlerAdvice{
	
	@Autowired
	ApplicationContext context
	
	public Object doInvoke(ExecutionCallback callback, Object target, Message message) throws Exception{
		log.info "payload: " + message.payload
		DirectChannel directChannel =  context.getBean("eventSourcingRedisChannel")
		directChannel.send(message)
		Object object = callback.execute()
		return object
	}

}
