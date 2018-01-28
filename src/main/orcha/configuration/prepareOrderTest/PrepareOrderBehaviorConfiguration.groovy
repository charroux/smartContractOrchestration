package configuration.prepareOrderTest

import org.springframework.beans.factory.annotation.Autowired
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
import orcha.lang.configuration.OrchaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.Retry
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@Configuration
@Slf4j
class PrepareOrderBehaviorConfiguration {
	
	@Autowired
	EventHandler customer
	
	@Autowired
	EventHandler delivery
	
	@Bean
	EventHandler oneTVFile(){
		EventHandler eventHandler = new EventHandler(name: "oneTVFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "oneTVFile.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.prepareOrder.Order", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application prepareAnOrder(){
		def program = new Application(name: "prepareAnOrder", specifications: "Prepare an order for a customer.", description: "Add an address for the delivery to an order.", language: "Orcha")
		def orchaAdapter = new OrchaServiceAdapter(input: customer, output: delivery)
		program.input = new Input(adapter: orchaAdapter)
		program.output = new Output(adapter: orchaAdapter)
		return program
	}
	
	@Bean
	EventHandler prepareOrderTestReport(){
		def eventHandler = new EventHandler(name: "prepareOrderTestReport")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'prepareOrderTestReport.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.prepareOrder.PreparedOrder", adapter: fileAdapter)
		return eventHandler
	}
	
}
