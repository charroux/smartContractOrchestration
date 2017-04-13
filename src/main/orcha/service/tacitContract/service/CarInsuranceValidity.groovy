package service.tacitContract.service

import groovy.util.logging.Slf4j;

import java.util.Date;

import service.tacitContract.domain.AvailableCarInsurance;
import service.tacitContract.domain.CarInsurance;

@Slf4j
class CarInsuranceValidity {
	
	AvailableCarInsurance check(CarInsurance carInsurance){
		log.info "receives : " + carInsurance
		Date validationDate = Calendar.getInstance().getTime()
		AvailableCarInsurance availableCarInsurance = new AvailableCarInsurance(carInsurance: carInsurance, validationDate: validationDate) 
		log.info "returns : " + availableCarInsurance
		return availableCarInsurance
	}

}
