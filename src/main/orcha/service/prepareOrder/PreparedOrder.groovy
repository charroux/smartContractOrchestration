package service.prepareOrder

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class PreparedOrder {
	
	String address		// address for delivery
	Order order

}
