package service.tripAgency

import groovy.transform.ToString

@ToString
class SelectedTaxi {

    int number
    String departureLocation
    Date departureDate
    String arrivalLocation
    int price

}
