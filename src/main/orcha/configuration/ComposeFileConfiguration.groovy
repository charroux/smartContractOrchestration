package configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.Retry
import orcha.lang.configuration.OutputFileAdapter.WritingMode

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import service.MyProgram;

@CircuitBreaker(numberOfFailuresBeforeOpening=2, intervalBeforeHalfOpening=3000L, orderInChain=1)
@Retry(maxNumberOfAttempts=3, intervalBetweenTheFirstAndSecondAttempt=1000L, intervalMultiplierBetweenAttemps=1, maximumIntervalBetweenAttempts=1000L, orderInChain=2)
class ProgramConfiguration extends Application{
}
 
@Configuration
class ComposeFileConfiguration {
		
	@Bean
	MyProgram myProgram(){
		return new MyProgram()
	}
	
	@Bean
	Application program(){
		def program = new ProgramConfiguration(name: "program", language: "Java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.MyProgram', method:'myMethod')
		program.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program
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
