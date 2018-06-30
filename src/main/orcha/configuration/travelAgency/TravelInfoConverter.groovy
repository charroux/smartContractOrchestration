package configuration.travelAgency

import java.text.SimpleDateFormat
import java.util.Date

import org.springframework.core.convert.converter.Converter
import org.springframework.integration.config.IntegrationConverter
import org.springframework.stereotype.Component
import service.travelAgency.SelectedTrain
import groovy.util.logging.Slf4j

@Component
@IntegrationConverter
@Slf4j
class TravelInfoConverter implements Converter<List, TravelInfo>	{

	@Override
	public TravelInfo convert(List source) {
		log.info "receives: " + source[0] + " " + source[1] + " " + source[2] 
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy")
		TravelInfo travelInfo = new TravelInfo(passenger: source[0].get("passenger"), departure: simpleDateFormat.parse(source[1].get("departure")), arrival: simpleDateFormat.parse(source[2].get("arrival")))
		log.info "returns: " + travelInfo
		return travelInfo
	}

}
