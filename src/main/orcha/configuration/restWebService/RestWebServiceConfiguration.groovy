package configuration.restWebService

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import groovy.util.logging.Slf4j
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.HttpAdapter
import orcha.lang.configuration.Input
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import service.restWebService.Preparation

@Configuration
@Slf4j
class RestWebServiceConfiguration {
	
	@Bean
	EventHandler remoteCustomerOverHttp(){
		EventHandler eventHandler = new EventHandler(name: "remoteCustomerOverHttp")
		def httpAdapter = new HttpAdapter(url: '/remoteCustomerOverHttp', method: HttpAdapter.Method.POST)
		eventHandler.input = new Input(mimeType: "application/json", type: "service.restWebService.Order", adapter: httpAdapter)
		return eventHandler
	}
	
	@Bean
	Application preparingOrder(){
		def program = new Application(name: "preparingOrder", specifications: "Prepare an order for a customer.", description: "Add an address for the delivery to an order.", language: "java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.restWebService.Preparation', method:'prepare')
		program.input = new Input(type: "service.restWebService.Order", adapter: javaAdapter)
		program.output = new Output(type: "service.restWebService.PreparedOrder", adapter: javaAdapter)
		return program
	}
	
	@Bean
	Preparation preparation(){
		return new Preparation()
	}
	
	@Bean
	EventHandler httpResponse(){
		def eventHandler = new EventHandler(name: "httpResponse")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'preparedOrder.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.restWebService.PreparedOrder", adapter: fileAdapter)
		return eventHandler
	}

}
