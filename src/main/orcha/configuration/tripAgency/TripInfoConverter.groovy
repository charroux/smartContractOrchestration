package configuration.tripAgency


import groovy.util.logging.Slf4j
import org.springframework.core.convert.converter.Converter
import org.springframework.integration.config.IntegrationConverter
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

@Component
@IntegrationConverter
@Slf4j
class TripInfoConverter implements Converter<List, TripInfo>	{

	@Override
	public TripInfo convert(List source) {
		log.info "receives: " + source[0] + " " + source[1] + " " + source[2] 
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy")
		TripInfo travelInfo = new TripInfo(passenger: source[0].get("passenger"), departure: simpleDateFormat.parse(source[1].get("departure")), arrival: simpleDateFormat.parse(source[2].get("arrival")))
		log.info "returns: " + travelInfo
		return travelInfo
	}

}
