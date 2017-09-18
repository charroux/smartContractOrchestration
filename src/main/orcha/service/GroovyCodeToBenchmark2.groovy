package service

import groovy.util.logging.Slf4j;
import org.springframework.transaction.annotation.Transactional

@Slf4j
class GroovyCodeToBenchmark2 {
	
	Integer method(Integer  i){
		log.info "GroovyCodeToBenchmark2 receives " + i 
		return i
	}

}
