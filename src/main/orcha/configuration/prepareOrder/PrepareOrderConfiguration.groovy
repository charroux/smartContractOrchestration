package configuration.prepareOrder

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import service.prepareOrder.OrderPreparation
import groovy.util.logging.Slf4j
import orcha.lang.configuration.Application
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.Retry
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@CircuitBreaker(numberOfFailuresBeforeOpening=2, intervalBeforeHalfOpening=2000L)
@Retry(maxNumberOfAttempts=3, intervalBetweenTheFirstAndSecondAttempt=5000L, intervalMultiplierBetweenAttemps=2, maximumIntervalBetweenAttempts=20000L)
class PrepareOrderApplication extends Application{
}

@Configuration
@Slf4j
class PrepareOrderConfiguration {
	
	@Bean
	EventHandler customer(){
		EventHandler eventHandler = new EventHandler(name: "customer")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "orderToPrepare.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.prepareOrder.Order", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application prepareOrder(){
		def program = new PrepareOrderApplication(name: "prepareOrder", specifications: "Prepare an order for a customer.", description: "Add an address for the delivery to an order.", language: "js")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.prepareOrder.OrderPreparation', method:'prepare')
		program.input = new Input(type: "service.prepareOrder.Order", adapter: javaAdapter)
		program.output = new Output(type: "service.prepareOrder.PreparedOrder", adapter: javaAdapter)
		return program
	}
	
	@Bean
	OrderPreparation orderPreparation(){
		return new OrderPreparation()
	}
	
	@Bean
	EventHandler delivery(){
		def eventHandler = new EventHandler(name: "delivery")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'preparedOrder.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.prepareOrder.PreparedOrder", adapter: fileAdapter)
		return eventHandler
	}

}
