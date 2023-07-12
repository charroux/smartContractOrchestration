package service.tripAgency

import groovy.util.logging.Slf4j

@Slf4j
class HotelSelection {
	
	service.tripAgency.SelectedHotel select(SelectedTrain selectedTrain){
		log.info "receives : " + selectedTrain
		return new service.tripAgency.SelectedHotel(passenger: selectedTrain.passenger, arrival: selectedTrain.arrival)
	}

}
