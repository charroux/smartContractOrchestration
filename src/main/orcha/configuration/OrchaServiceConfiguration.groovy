package configuration

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
import service.OrchaGroovyService

@Configuration
class OrchaServiceConfiguration {
	
	
	@Bean
	EventHandler orchaProgram(){
		def eventHandler = new EventHandler(name: "orchaProgram")
		def middlewareAdapter = new MessagingMiddlewareAdapter()
		eventHandler.input = new Input(mimeType: "text/plain", type: "java.lang.Integer", adapter: middlewareAdapter)
		return eventHandler
	}
	
	@Bean
	OrchaGroovyService orchaGroovyService(){
		return new OrchaGroovyService()
	}
	
	@Bean
	Application orchaService(){
		def code1Application = new Application(name: "orchaService", language: "Groovy")
		def groovyCode1Adapter = new JavaServiceAdapter(javaClass: 'service.OrchaGroovyService', method:'method')
		def code1Input = new Input(type: "java.lang.Integer", adapter: groovyCode1Adapter)
		code1Application.input = code1Input
		def code1Output = new Output(type: "java.lang.Integer", adapter: groovyCode1Adapter)
		code1Application.output = code1Output
		return code1Application
	}
		
	@Bean
	EventHandler orchaServiceOutputFile(){
		def eventHandler = new EventHandler(name: "orchaServiceOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'orchaServiceOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}


}
