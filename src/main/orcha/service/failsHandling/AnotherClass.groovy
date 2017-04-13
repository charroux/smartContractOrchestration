package service.failsHandling

import groovy.util.logging.Slf4j;

@Slf4j
class AnotherClass {
	
	String method(String s){
		log.info "AnotherClass receives " + s
		//throw new RuntimeException("Wrong address");
		return s
	}

}
