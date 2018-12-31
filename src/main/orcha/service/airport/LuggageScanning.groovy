package service.airport

import groovy.util.logging.Slf4j

@Slf4j
class LuggageScanning {
	
	LuggageScan scan(Luggage luggage) {
		log.info  "receives: " + luggage
		LuggageScan luggageScan = new LuggageScan(luggage: luggage, alert: true)
		log.info  "returns: " + luggageScan
		return luggageScan
	}

}
