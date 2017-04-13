package service.tacitContract.service

import groovy.util.logging.Slf4j;
import service.tacitContract.domain.AvailableDrivingLicence;
import service.tacitContract.domain.DrivingLicence;

@Slf4j
class DrivingLicenceValidity {
	
	AvailableDrivingLicence check(DrivingLicence drivingLicence){
		log.info "receives : " + drivingLicence
		AvailableDrivingLicence availableDrivingLicence = new AvailableDrivingLicence(drivingLicence: drivingLicence, availability: true)
		log.info "returns : " + availableDrivingLicence
		return availableDrivingLicence
	}
	
}
