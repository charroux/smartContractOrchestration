package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import groovy.util.logging.Slf4j;
import orcha.lang.configuration.Application
import orcha.lang.configuration.State

@Slf4j
class ObjectToApplicationTransformer {
	
	Application application
	
	Application transform(Object object){
		log.info "receives  (" + this + ") : " + object
		Application clonedApplication = application.clone()
		clonedApplication.output.value = object
		clonedApplication.state = State.TERMINATED
		log.info "returns : " + clonedApplication
		return clonedApplication
	}

}
