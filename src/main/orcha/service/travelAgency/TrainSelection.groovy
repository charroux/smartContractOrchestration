package service.travelAgency

import configuration.travelAgency.TravelInfo
import groovy.util.logging.Slf4j

@Slf4j
class TrainSelection {
	
	SelectedTrain select(TravelInfo travelInfo) {
		log.info "receives : " + travelInfo
		return new SelectedTrain(number: 1234, passenger: travelInfo.passenger, departure: travelInfo.departure, arrival: travelInfo.arrival)
	}
	
}
