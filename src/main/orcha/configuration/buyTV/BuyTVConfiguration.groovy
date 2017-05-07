package configuration.buyTV

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.Output

@Configuration
class BuyTVConfiguration {
	
	@Bean
	EventHandler buyer(){
		def buyer = new EventHandler(name: "buyer")
		buyer.input = new Input(mimeType: "application/json", type: "service.buyTV.TVSpecifications")
		return buyer
	}
	
	@Bean
	Application buyTV(){
		def buyTV = new Application(name: "buyTV", specifications: "..." )
		buyTV.input = new Input(type: "service.buyTV.TVSpecifications")
		buyTV.output = new Output(type: "service.buyTV.TVInvoice")
		return buyTV
	}
	
	@Bean
	Application deliverTV(){
		def deliverTV = new Application(name: "deliverTV", specifications: "..." )
		deliverTV.input = new Input(type: "service.buyTV.TVInvoice")
		deliverTV.output = new Output(type: "service.buyTV.DeliveryReceipt")
		return deliverTV
	}

	@Bean
	EventHandler seller(){
		def seller = new EventHandler(name: "seller")
		seller.input = new Input(mimeType: "application/json", type: "service.buyTV.DeliveryReceipt")
		return seller
	}
	
}
