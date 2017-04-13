package service.tacitContract.service

import groovy.util.logging.Slf4j;
import orcha.lang.configuration.Application;

import org.springframework.beans.factory.annotation.Autowired;

import service.tacitContract.domain.AvailableCarInsurance;
import service.tacitContract.domain.AvailableDrivingLicence;
import service.tacitContract.domain.Taxi;

@Slf4j
class TaxiValidity {

	Taxi authorize(def arguments = []){
		log.info "receives : " + arguments
		AvailableDrivingLicence availableDrivingLicence
		AvailableCarInsurance availableCarInsurance
		if(arguments[0] instanceof AvailableDrivingLicence){
			availableDrivingLicence = arguments[0]
			availableCarInsurance = arguments[1]
		} else {
			availableDrivingLicence = arguments[1]
			availableCarInsurance = arguments[0]
		}
		
		Taxi taxi = new Taxi(availableDrivingLicence: availableDrivingLicence, availableCarInsurance: availableCarInsurance)
		log.info "returns : " + taxi
		return taxi
	}
	
}
