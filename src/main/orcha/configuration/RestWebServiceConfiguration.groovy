package configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.HttpAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.HttpAdapter.Method
import orcha.lang.configuration.Input
import orcha.lang.configuration.OutputFileAdapter.WritingMode

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import service.MyProgram

@Configuration
class RestWebServiceConfiguration {
	
	@Bean
	EventHandler input(){
		def eventHandler = new EventHandler(name: "input")
		def restAdapter = new HttpAdapter(url: '/microserviceUrl', method: Method.PUT)
		eventHandler.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: restAdapter)
		return eventHandler
	}
	
	@Bean
	MyProgram myProgram(){
		return new MyProgram()
	}
	
	@Bean
	Application javaCode(){
		def code1Application = new Application(name: "javaCode", language: "Java")
		def javaCode1Adapter = new JavaServiceAdapter(javaClass: 'service.MyProgram', method:'myMethod')
		def code1Input = new Input(type: "java.lang.Integer", adapter: javaCode1Adapter)
		code1Application.input = code1Input
		def code1Output = new Output(type: "java.lang.Integer", adapter: javaCode1Adapter)
		code1Application.output = code1Output
		return code1Application
	}
	
	@Bean
	EventHandler resultFile(){
		def eventHandler = new EventHandler(name: "resultFile")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'output.txt', appendNewLine: true, writingMode: WritingMode.APPEND)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
}
