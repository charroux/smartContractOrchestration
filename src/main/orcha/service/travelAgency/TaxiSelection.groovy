package service.travelAgency

import groovy.util.logging.Slf4j

@Slf4j
class TaxiSelection {
	
	SelectedTaxi select(def travelInfo){
		log.info "receives : " + travelInfo[0] + " " + travelInfo[1]
		return new SelectedTaxi(hotel: travelInfo[0], train: travelInfo[1])
	}

}