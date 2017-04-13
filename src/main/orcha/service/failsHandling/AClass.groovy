package service.failsHandling

import groovy.util.logging.Slf4j;

@Slf4j
class AClass {
	
	String method(String s){
		log.info "AClass receives " + s
		//throw new RuntimeException("Wrong method in AClass");
		return s
	}

}
