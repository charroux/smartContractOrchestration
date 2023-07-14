package service.tripAgency

import groovy.transform.ToString

@ToString
class SelectedTaxi {

    int bookingNumber
    int number
    String departureLocation
    Date departureDate
    String arrivalLocation
    int price
    String selectedTaxiContractAddress

}
