package service.qos

import groovy.util.logging.Slf4j

@Slf4j
class EnQueuedService {
	
	static int numberOfCalls = 0
	
	String myMethod(String s){		
		log.info "call number: " + numberOfCalls
		numberOfCalls++
		log.info "receives: " + s
		log.info "returns: " + s
		return s
	}
	

}
