package service.orchaService

import groovy.util.logging.Slf4j

@Slf4j
class OrchaGroovyService {
	
	Order method(Product product){
		log.info "OrchaGroovyService receives " + product
		Order order = new Order(number: 1, product: product)
		return order
	}

}
