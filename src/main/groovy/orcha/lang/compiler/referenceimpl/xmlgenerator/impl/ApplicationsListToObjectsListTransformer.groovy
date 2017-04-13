package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import groovy.util.logging.Slf4j;
import orcha.lang.configuration.Application

@Slf4j
class ApplicationsListToObjectsListTransformer {
	
	 def transform(List<Application> applications){
		 
		 log.info "receives : " + applications
		
		 def objectsList = []
		 applications.each{
			objectsList.add(it.output.value)
		 }
		 
		 log.info "returns : " + objectsList
		
		 return objectsList
	}

}
