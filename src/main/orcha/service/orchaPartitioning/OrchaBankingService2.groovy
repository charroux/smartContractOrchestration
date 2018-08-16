package service.orchaPartitioning

import groovy.util.logging.Slf4j

@Slf4j
class OrchaBankingService2 {
	
	BankingTransaction process(Order order){
		log.info "OrchaBankingService2 receives " + order
		return new BankingTransaction(date: Calendar.instance.getTime(), order: order)
	}

}
