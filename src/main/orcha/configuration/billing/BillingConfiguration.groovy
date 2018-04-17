package configuration.billing

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@Configuration
class BillingConfiguration {
	
	@Bean
	EventHandler preparedOrder(){
		EventHandler eventHandler = new EventHandler(name: "preparedOrder")
		def fileAdapter = new InputFileAdapter(directory: 'data/output', filenamePattern: "preparedOrder.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.prepareOrder.PreparedOrder", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application billing(){
		def jsApp = new Application(name: "billing", language: "js", specifications: "Billing of an order", description: "Billing of an order")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/billing/billing.js')
		jsApp.input = new Input(type: "service.prepareOrder.PreparedOrder", adapter: scriptAdapter)
		jsApp.output = new Output(type: "service.billing.Bill", adapter: scriptAdapter)
		return jsApp
	}
	
	@Bean
	EventHandler accounting(){
		def eventHandler = new EventHandler(name: "accounting")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'billing.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.billing.Bill", adapter: fileAdapter)
		return eventHandler
	}
	
}
