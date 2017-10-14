package configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import service.GroovyEventFilteringCode
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@Configuration
class EventFilteringExampleConfiguration {
	
	@Bean
	EventHandler eventFilteringInputFile(){
		def eventHandler = new EventHandler(name: "eventFilteringInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "eventFilteringInputFile.txt")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	GroovyEventFilteringCode groovyEventFilteringCode(){
		return new GroovyEventFilteringCode()
	}
	
	@Bean
	Application eventFilteringCode(){
		def codeApplication = new Application(name: "eventFilteringCode", language: "Groovy", description:"Receives and integer and returns the opposite value")
		def groovyCodeAdapter = new JavaServiceAdapter(javaClass: 'service.GroovyEventFilteringCode', method:'method')
		def codeInput = new Input(type: "java.lang.Integer", adapter: groovyCodeAdapter)
		codeApplication.input = codeInput
		def codeOutput = new Output(type: "java.lang.Integer", adapter: groovyCodeAdapter)
		codeApplication.output = codeOutput
		return codeApplication
	}
	
	@Bean
	EventHandler eventFilteringOutputFile(){
		def eventHandler = new EventHandler(name: "eventFilteringOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'eventFilteringOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}

}