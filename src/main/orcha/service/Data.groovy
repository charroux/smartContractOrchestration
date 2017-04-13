package service

import groovy.util.logging.Slf4j;

@Slf4j
class Data {
	
	int i
	
	int data(){
		i = 1
		log.info "Data returns " + i 
		return i
	}
}
