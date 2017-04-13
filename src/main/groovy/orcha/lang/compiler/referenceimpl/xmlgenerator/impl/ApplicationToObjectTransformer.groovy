package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import groovy.util.logging.Slf4j;
import orcha.lang.configuration.Application

@Slf4j
class ApplicationToObjectTransformer {
	
	Object transform(Application application){
		log.info "receives : " + application
		log.info "returns : " + application.output.value
		return application.output.value
	}

}
