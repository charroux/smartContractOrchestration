package configuration.javascript

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
class JavascriptExampleConfiguration {

	@Bean
	EventHandler javascriptServiceInputFile(){
		def eventHandler = new EventHandler(name: "javascriptServiceInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "javascriptServiceInputFile.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.javascript.Person", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application javascriptService(){
		def jsApp = new Application(name: "javascriptService", language: "js", specifications: "Get a person as an argument and return her address. Argument: service.javascript.Person. Return: service.javascript.Address", description: "Get a person as an argument and return her address.")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/javascript/javascriptService.js')
		jsApp.input = new Input(type: "service.javascript.Person", adapter: scriptAdapter)
		jsApp.output = new Output(type: "service.javascript.Employee", adapter: scriptAdapter)
		return jsApp
	}
	
	@Bean
	EventHandler javascriptServiceOutputFile(){
		def eventHandler = new EventHandler(name: "javascriptServiceOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'javascriptServiceOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.javascript.Employee", adapter: fileAdapter)
		return eventHandler
	}

}
