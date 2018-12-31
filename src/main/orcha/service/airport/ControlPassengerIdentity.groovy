package service.airport

import groovy.util.logging.Slf4j

@Slf4j
class ControlPassengerIdentity {
	
	Passenger control(Person person) {
		log.info  "receives: " + person
		Passenger passenger = new Passenger(name: person.name, identifier: 1)
		log.info  "returns: " + passenger
		return passenger
	}

}
