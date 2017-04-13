package service.failsHandling

import groovy.util.logging.Slf4j;

@Slf4j
class AlternativeClass {
	
	String method(String s){
		log.info "AlternativeClass receives " + s
		return s
	}

}
