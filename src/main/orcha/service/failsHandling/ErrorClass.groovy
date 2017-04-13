package service.failsHandling

import groovy.util.logging.Slf4j;

@Slf4j
class ErrorClass {
	
	void method(def s){
		log.info "ErrorClass receives " + s
	}

}
