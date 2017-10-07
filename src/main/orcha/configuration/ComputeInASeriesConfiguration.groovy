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

import service.Program1;
import service.Program2

@Configuration
class ComputeInASeriesConfiguration {
	
	@Bean
	EventHandler computesInSeriesInputFile(){
		def eventHandler = new EventHandler(name: "computesInSeriesInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "computesInSeriesInputFile.txt")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
		
	@Bean
	Program1 program1(){
		return new Program1()
	}
	
	@Bean
	Application firstProgram(){
		def program1 = new Application(name: "firstProgram", language: "Java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.Program1', method:'myMethod')
		program1.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program1.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program1
	}
	
	@Bean
	Program2 program2(){
		return new Program2()
	}
	
	@Bean
	Application secondProgram(){
		def program2 = new Application(name: "secondProgram", language: "Java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.Program2', method:'myMethod')
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
	
	@Bean
	EventHandler computesInSeriesOutputFile(){
		def eventHandler = new EventHandler(name: "computesInSeriesOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'computesInSeriesOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
	
}
