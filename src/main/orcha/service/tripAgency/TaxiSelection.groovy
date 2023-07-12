package service.tripAgency

import groovy.util.logging.Slf4j

@Slf4j
class TaxiSelection {
	
	service.tripAgency.SelectedTaxi select(def travelInfo){
		log.info "receives : hotel " + travelInfo[0] + ", train " + travelInfo[1]
		return new service.tripAgency.SelectedTaxi(hotel: travelInfo[0], train: travelInfo[1])
	}

}
