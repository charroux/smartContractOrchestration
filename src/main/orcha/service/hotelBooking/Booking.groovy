package service.hotelBooking

import groovy.transform.ToString

@ToString
class Booking implements Serializable{
	
	Date begin
	Date end
	int numberOfBeds
	
	public Booking() {
		super()
	}
	
	public Booking(Date begin, Date end, int numberOfBeds) {
		super();
		this.begin = begin;
		this.end = end;
		this.numberOfBeds = numberOfBeds;
	}

}
