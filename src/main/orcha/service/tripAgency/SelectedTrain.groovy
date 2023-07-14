package service.tripAgency

import groovy.transform.ToString

@ToString
class SelectedTrain {

	int bookingNumber
	int number
	String destination
	Date departure
	Date arrival
	int ticketPrice
	String selectedTrainContractAddress

}
