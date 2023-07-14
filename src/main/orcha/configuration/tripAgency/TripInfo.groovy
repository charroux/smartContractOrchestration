package configuration.tripAgency

import groovy.transform.ToString
import service.tripAgency.SelectedHotel
import service.tripAgency.SelectedTaxi
import service.tripAgency.SelectedTrain

import java.text.SimpleDateFormat

@ToString
class TripInfo {

	String travelDestination
	String traveller
	Date departure
	Date arrival
	int maximumPrice
	int billableAmount
	int creditBalance

	String booking = "PENDING"

	SelectedTrain selectedTrain
	SelectedHotel selectedHotel
	SelectedTaxi selectedTaxi

}
