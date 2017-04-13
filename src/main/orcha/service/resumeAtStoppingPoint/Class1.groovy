package service.resumeAtStoppingPoint

import groovy.util.logging.Slf4j;

@Slf4j
class Class1 {
	
	Integer method1(Integer i){
		log.info "receives : " + i
		Thread.sleep(5000)
		return i
	}

}
