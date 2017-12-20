package service

import groovy.util.logging.Slf4j

@Slf4j
class OrderPreparationTest {
	
	PreparedOrderTest prepare(Order order){
		log.info  "receives: " + order
		PreparedOrderTest preparedOrder = new PreparedOrderTest(address: "10 av des Champs-Elysées, 75008 Paris", order: order, delay: 11)
		log.info  "returns: " + preparedOrder
		return preparedOrder
	}

}
