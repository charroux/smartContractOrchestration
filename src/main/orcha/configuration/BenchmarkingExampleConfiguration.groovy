package configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import service.GroovyCode1
import service.GroovyCodeToBenchmark1
import service.GroovyCodeToBenchmark2

@Configuration
class BenchmarkingExampleConfiguration {
	
	@Bean
	EventHandler benchmarkingInputFile(){
		def eventHandler = new EventHandler(name: "benchmarkingInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "benchmarkingData.txt")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	GroovyCodeToBenchmark1 groovyCodeToBenchmark1(){
		return new GroovyCodeToBenchmark1()
	}
	
	@Bean
	Application codeToBenchmark1(){
		def code1Application = new Application(name: "codeToBenchmark1", language: "Groovy", description:"Receives and integer and returns the opposite value")
		def groovyCode1Adapter = new JavaServiceAdapter(javaClass: 'service.GroovyCodeToBenchmark1', method:'method')
		def code1Input = new Input(type: "java.lang.Integer", adapter: groovyCode1Adapter)
		code1Application.input = code1Input
		def code1Output = new Output(type: "java.lang.Integer", adapter: groovyCode1Adapter)
		code1Application.output = code1Output
		return code1Application
	}
	
	@Bean
	GroovyCodeToBenchmark2 groovyCodeToBenchmark2(){
		return new GroovyCodeToBenchmark2()
	}
	
	@Bean
	Application codeToBenchmark2(){
		def code1Application = new Application(name: "codeToBenchmark2", language: "Groovy", description: "Return the received integer")
		def groovyCode1Adapter = new JavaServiceAdapter(javaClass: 'service.GroovyCodeToBenchmark2', method:'method')
		def code1Input = new Input(type: "java.lang.Integer", adapter: groovyCode1Adapter)
		code1Application.input = code1Input
		def code1Output = new Output(type: "java.lang.Integer", adapter: groovyCode1Adapter)
		code1Application.output = code1Output
		return code1Application
	}
	
	@Bean
	EventHandler benchmarkingOutputFile(){
		def eventHandler = new EventHandler(name: "benchmarkingOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'benchmarkingOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}

}
