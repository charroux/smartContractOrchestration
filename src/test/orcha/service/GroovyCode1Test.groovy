package service

import groovy.util.logging.Slf4j;
import org.springframework.transaction.annotation.Transactional

@Slf4j
class GroovyCode1Test {
	
	Integer method1(Integer  i){
		log.info "GroovyCode1 receives " + i 
		return -i
	}

}
