package service.order

import groovy.util.logging.Slf4j;
import service.order.Order

@Slf4j
class VendorOrderConverter {
	
	static int i = 0
	
	SpecificOrder convert(Order order){
		/*if(i<1){
			println i
			i++
			throw new RuntimeException()
		}
		
		println "oooookkkkkkkkkkkkkk"
		*/
		log.info  "receives: " + order
		SpecificOrder specificOrder = new SpecificOrder(number: order.number, product: order.product.specification)
		log.info  "returns: " + specificOrder
		return specificOrder
	}

	
}

