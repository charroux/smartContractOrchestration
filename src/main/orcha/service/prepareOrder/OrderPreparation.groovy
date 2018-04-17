package service.prepareOrder

import groovy.util.logging.Slf4j

@Slf4j
class OrderPreparation {
	
	PreparedOrder prepare(Order order){
		log.info  "receives: " + order
		PreparedOrder preparedOrder = new PreparedOrder(address: "10 av des Champs-Elysées, 75008 Paris", order: order, delay: 3)
		log.info  "returns: " + preparedOrder
		return preparedOrder
	}

}
