package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import groovy.util.logging.Slf4j;
import orcha.lang.configuration.Application
import orcha.lang.configuration.State
import orcha.lang.configuration.Error

@Slf4j
class ErrorToApplicationTransformer {
	
	Application application
	
	Application transform(Error error){
		log.info "receives (" + this + ") : " + error
		Application clonedApplication = application.clone()
		clonedApplication.error = error
		clonedApplication.state = State.FAILED
		log.info "returns : " + clonedApplication
		return clonedApplication
	}

}
