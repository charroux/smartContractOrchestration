package service.hotelBooking

import groovy.transform.ToString

/**
 * @author benoit.charroux
 *
 */
@ToString
class BookingReceipt {
	
	Booking booking
	boolean booked
	public BookingReceipt(Booking booking, boolean booked) {
		super();
		this.booking = booking;
		this.booked = booked;
	}
	
}
	

