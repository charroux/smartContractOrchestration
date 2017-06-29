package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import groovy.util.logging.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter

@Slf4j
class StopAdapter {
	
	@Autowired
	SourcePollingChannelAdapter mongoInboundAdapter
	
	void stop(){
		mongoInboundAdapter.stop()
		log.info "mongoInboundAdapter has been stopped"
	}

}
