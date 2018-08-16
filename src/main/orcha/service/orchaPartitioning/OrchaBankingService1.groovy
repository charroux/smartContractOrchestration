package service.orchaPartitioning

import groovy.util.logging.Slf4j

@Slf4j
class OrchaBankingService1 {
	
	BankingTransaction process(Order order){
		log.info "OrchaBankingService1 receives " + order
		return new BankingTransaction(date: Calendar.instance.getTime(), order: order)
	}

}
