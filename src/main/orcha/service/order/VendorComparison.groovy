package service.order

import groovy.util.logging.Slf4j;

@Slf4j
class VendorComparison {
	
	Bill compare(def bills){
		
		log.info "receives : " + bills
		
		Bill best
		float price = Float.MAX_VALUE
		bills.each{ bill ->
			if(bill.price < price){
				best = bill
				price = bill.price
			}
		}
		
		log.info "returns : " + best
		
		return best
	}

}
