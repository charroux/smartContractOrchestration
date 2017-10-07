package service

import groovy.util.logging.Slf4j;
import org.springframework.transaction.annotation.Transactional

@Slf4j
class GroovyEventFilteringCode {
	
	Integer method(Integer  i){
		log.info "GroovyEventFilteringCode receives " + i 
		return -i
	}

}
