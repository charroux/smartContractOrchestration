package service.airport

import groovy.util.logging.Slf4j

@Slf4j
class AlertService {
	
	SecurityAlert alert(LuggageScan luggageScan) {
		log.info  "receives: " + luggageScan
		SecurityAlert securityAlert = new SecurityAlert(luggageScan: luggageScan)
		log.info  "returns: " + securityAlert
		return securityAlert
	}

}
