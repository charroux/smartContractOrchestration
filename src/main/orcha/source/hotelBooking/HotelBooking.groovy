package source.hotelBooking

title "hotel booking"

receive booking from onlineBookingDemand
compute book with booking.value
when "book terminates" 
send book.result to bookingRespons
