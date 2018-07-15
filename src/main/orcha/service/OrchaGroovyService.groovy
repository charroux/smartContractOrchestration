package service

import groovy.util.logging.Slf4j

@Slf4j
class OrchaGroovyService {
	
	Integer method(Integer  i){
		log.info "OrchaGroovyService receives " + i
		return -i
	}

}
