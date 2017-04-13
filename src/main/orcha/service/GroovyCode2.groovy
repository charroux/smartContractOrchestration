package service

import groovy.util.logging.Slf4j;
import org.springframework.transaction.annotation.Transactional

@Slf4j
class GroovyCode2 {
	
	Integer method2(Integer  i){
		log.info "GroovyCode2 receives " + i 
		return i
	}

}
