package configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.HttpAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.HttpAdapter.Method
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RestWebServiceCallConfiguration {
	
	@Bean
	EventHandler user(){
		def eventHandler = new EventHandler(name: "user")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "*.txt")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application remoteService(){
		def program = new Application(name: "remoteService", language: "Java")
		def restAdapter = new HttpAdapter(url: 'http://localhost:8181/remoteServiceUrl', method: Method.PUT)
		program.output = new Output(mimeType: "application/json", type: "java.lang.Integer", adapter: restAdapter)
		program.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: restAdapter)
		return program
	}
	
	@Bean
	EventHandler aFile(){
		def eventHandler = new EventHandler(name: "aFile")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'aFile.txt', appendNewLine: true, writingMode: WritingMode.APPEND)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
}
