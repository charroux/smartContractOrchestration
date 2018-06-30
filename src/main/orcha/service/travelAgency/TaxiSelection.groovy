package service.travelAgency

import groovy.util.logging.Slf4j

@Slf4j
class TaxiSelection {
	
	SelectedTaxi select(def travelInfo){
		log.info "receives : " + selectedTrain
		return new SelectedTaxi(train: travelInfo[0], hotel: travelInfo[1])
	}

}
