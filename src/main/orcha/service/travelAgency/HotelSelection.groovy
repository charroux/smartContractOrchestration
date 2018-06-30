package service.travelAgency

import groovy.util.logging.Slf4j

@Slf4j
class HotelSelection {
	
	SelectedHotel select(SelectedTrain selectedTrain){
		log.info "receives : " + selectedTrain
		return new SelectedHotel(passenger: selectedTrain.passenger, arrival: selectedTrain.arrival)
	}

}
