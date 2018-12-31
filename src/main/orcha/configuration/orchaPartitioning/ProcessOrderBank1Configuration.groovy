package configuration.orchaPartitioning

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import service.orchaPartitioning.BankingService1
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.MessagingMiddlewareAdapter
import orcha.lang.configuration.Output

@Configuration
class ProcessOrderBank1Configuration {
	
	@Bean
	EventHandler bankingOrder(){
		def eventHandler = new EventHandler(name: "bankingOrder")
		def middlewareAdapter = new MessagingMiddlewareAdapter(partitioned: true, instanceCount: 2, partitionNumber: 1)
		eventHandler.input = new Input(mimeType: "application/json", type: "service.orchaPartitioning.Order", adapter: middlewareAdapter)
		return eventHandler
	}
	
	@Bean
	BankingService1 bankingService1(){
		return new BankingService1()
	}
	
	@Bean
	Application processOrderBank(){
		def application = new Application(name: "processOrderBank", language: "Groovy")
		def groovyCode1Adapter = new JavaServiceAdapter(javaClass: 'service.orchaPartitioning.BankingService1', method:'process')
		def input = new Input(type: "service.orchaPartitioning.Order", adapter: groovyCode1Adapter)
		application.input = input
		def output = new Output(type: "service.orchaPartitioning.BankingTransaction", adapter: groovyCode1Adapter)
		application.output = output
		return application
	}
	
	@Bean
	EventHandler bankingTransaction(){
		def eventHandler = new EventHandler(name: "bankingTransaction")
		def middlewareAdapter = new MessagingMiddlewareAdapter()
		eventHandler.output = new Output(mimeType: "application/json", type: "service.orchaPartitioning.BankingTransaction", adapter: middlewareAdapter)
		return eventHandler
	}


}
