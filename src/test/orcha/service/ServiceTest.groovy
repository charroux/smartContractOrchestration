package service

import groovy.util.logging.Slf4j

@Slf4j
class ServiceTest {
	
	static int numberOfCalls = 0
	
	String myMethod(String s){
		
		log.info "call number: " + numberOfCalls
		log.info "receives: " + s
		
		if(numberOfCalls < 2){			
			numberOfCalls++
			log.info "throw exception"
			throw new RuntimeException();
		}
		
		log.info "returns: " + s
		return s
	}
	

}
