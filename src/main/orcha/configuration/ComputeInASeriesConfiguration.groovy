package configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import orcha.lang.configuration.OutputFileAdapter
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import service.MyProgram1;
import service.MyProgram2

@Configuration
class ComputeInASeriesConfiguration {
		
	@Bean
	MyProgram1 myProgram1(){
		return new MyProgram1()
	}
	
	@Bean
	Application program1(){
		def program1 = new Application(name: "program1", language: "Java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.MyProgram1', method:'myMethod')
		program1.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program1.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program1
	}
	
	@Bean
	MyProgram2 myProgram2(){
		return new MyProgram2()
	}
	
	@Bean
	Application program2(){
		def program2 = new Application(name: "program2", language: "Java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.MyProgram2', method:'myMethod')
		program2.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program2.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program2
	}

	@Bean
	EventHandler inputFile(){
		def eventHandler = new EventHandler(name: "inputFile")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "*.txt")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler outputFile(){
		def eventHandler = new EventHandler(name: "outputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'output.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
}
