package service.failsHandling

import groovy.util.logging.Slf4j;

@Slf4j
class ServiceComparison {
	
	def compare(List<String> strings){
		log.info "ServiceComparison receives : " + strings
		return strings
	}

}
