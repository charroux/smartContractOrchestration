package service

import groovy.util.logging.Slf4j;
import org.springframework.transaction.annotation.Transactional

@Slf4j
class GroovyCodeToBenchmark1 {
	
	Integer method(Integer  i){
		log.info "GroovyCodeToBenchmark1 receives " + i 
		return -i
	}

}
