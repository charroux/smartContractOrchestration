package service.blockchain.basicLog

import groovy.util.logging.Slf4j;

@Slf4j
class Service4Class {
	
	Person method(Person person){
		log.info  "receives: " + person
		person.age = 21
		return person
	}

}
