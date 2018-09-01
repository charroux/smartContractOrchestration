package service.orchaPartitioning

import groovy.util.logging.Slf4j

@Slf4j
class BankingService1 {
	
	BankingTransaction process(Order order){
		log.info "BankingService1 receives " + order
		return new BankingTransaction(date: Calendar.instance.getTime(), order: order)
	}

}
