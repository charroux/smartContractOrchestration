package service.tripAgency

import groovy.transform.ToString

@ToString
class SelectedHotel {

	int bookingNumber
	String address
	Date arrival
	int roomNumber
	int roomPrice
	String selectedHotelContractAddress
	
}
