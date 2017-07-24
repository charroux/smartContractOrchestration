package configuration.prepareOrder

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import groovy.util.logging.Slf4j
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

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
		def program = new Application(name: "prepareOrder", specifications: "Prepare an order for a customer. Argument: service.order.Order. Return: service.order.SpecificOrder", description: "Convert a specific vendor order into a generic one.", language: "js")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/prepareOrder/prepareOrder.js')
		program.input = new Input(type: "service.prepareOrder.Order", adapter: scriptAdapter)
		program.output = new Output(type: "service.prepareOrder.PreparedOrder", adapter: scriptAdapter)
		return program
	}
	
	@Bean
	EventHandler delivery(){
		def eventHandler = new EventHandler(name: "delivery")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'preparedOrder.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.prepareOrder.PreparedOrder", adapter: fileAdapter)
		return eventHandler
	}

}
