package service

import groovy.util.logging.Slf4j;

import org.springframework.beans.factory.annotation.Autowired

@Slf4j
class ClassWithAVariableProcess {
	
	@Autowired
	Data data
	
	Integer method(Integer i){
		log.info "ClassWithAVariableProcess adds " + i + " to " + data.data()
		return data.data() + i
	}

}