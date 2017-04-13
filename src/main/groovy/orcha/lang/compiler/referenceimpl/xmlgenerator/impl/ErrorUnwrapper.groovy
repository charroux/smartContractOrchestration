package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import groovy.util.logging.Slf4j;

import orcha.lang.configuration.Error

import org.springframework.integration.transformer.MessageTransformationException
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;

@Slf4j
class ErrorUnwrapper {
	
	Error transform(MessageHandlingException errorMessage){
		log.info "ErrorUnwrapper receives " + errorMessage
		return new Error(originalMessage: errorMessage.failedMessage.payload, exception: errorMessage.cause, message: errorMessage.message)
	}
	
	Error transform(MessageTransformationException errorMessage){
		log.info "ErrorUnwrapper receives " + errorMessage
		Error error = new Error(originalMessage: errorMessage.failedMessage.payload, exception: errorMessage.cause, message: errorMessage.message)
		log.info "error : " + error
		return error
	}
	
	/*public def transform(MessageHandlingException errorMessage) {
		log.info "ErrorUnwrapper receives " + errorMessage
		log.info "ErrorUnwrapper receives " + errorMessage.failedMessage.payload
		return errorMessage.failedMessage.payload
		//return ((MessagingException) errorMessage.getPayload()).getFailedMessage();
	}*/

}
