/**
 * 
 */

function hotelBooking(booking) {
	var Booking = Java.type("service.hotelBooking.Booking");	// tested with the java's standard javascript engine embedded into java 1.8.
	var book = new Booking(booking.begin, booking.end, booking.numberOfBeds);
	var BookingReceipt = Java.type("service.hotelBooking.BookingReceipt");
	return new BookingReceipt(book, true);
}

hotelBooking(payload);