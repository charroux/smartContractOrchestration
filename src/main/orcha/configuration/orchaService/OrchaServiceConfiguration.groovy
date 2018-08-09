package configuration.orchaService

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.MessagingMiddlewareAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import service.GroovyCodeToBenchmark1
import service.GroovyCodeToBenchmark2
import service.orchaService.OrchaGroovyService

@Configuration
class OrchaServiceConfiguration {
	
	@Bean
	EventHandler orchaProgramSource(){
		def eventHandler = new EventHandler(name: "orchaProgramSource")
		def middlewareAdapter = new MessagingMiddlewareAdapter()
		eventHandler.input = new Input(mimeType: "application/json", type: "service.orchaService.Product", adapter: middlewareAdapter)
		return eventHandler
	}
	
	@Bean
	OrchaGroovyService orchaGroovyService(){
		return new OrchaGroovyService()
	}
	
	@Bean
	Application orchaService(){
		def code1Application = new Application(name: "orchaService", language: "Groovy")
		def groovyCode1Adapter = new JavaServiceAdapter(javaClass: 'service.orchaService.OrchaGroovyService', method:'method')
		def code1Input = new Input(type: "service.orchaService.Product", adapter: groovyCode1Adapter)
		code1Application.input = code1Input
		def code1Output = new Output(type: "service.orchaService.Order", adapter: groovyCode1Adapter)
		code1Application.output = code1Output
		return code1Application
	}
	
	@Bean
	EventHandler orchaProgramDestination(){
		def eventHandler = new EventHandler(name: "orchaProgramDestination")
		def middlewareAdapter = new MessagingMiddlewareAdapter()
		eventHandler.output = new Output(mimeType: "application/json", type: "service.orchaService.Order", adapter: middlewareAdapter)
		return eventHandler
	}
	
}
