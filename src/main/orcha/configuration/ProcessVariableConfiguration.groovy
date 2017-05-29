package configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import service.ClassWithAVariableProcess
import service.Data
import service.MyProgram;

@Configuration
class ProcessVariableConfiguration {
		
	@Bean
	ClassWithAVariableProcess classWithAVariableProcess(){
		return new ClassWithAVariableProcess()
	}
	
	@Bean
	Data data(){
		return new Data()
	}
	
	@Bean
	Application codeWithAVariableProcess(){
		def program = new Application(name: "codeWithAVariableProcess", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.ClassWithAVariableProcess', method:'method')
		program.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program
	}

	@Bean
	EventHandler entering(){
		def eventHandler = new EventHandler(name: "entering")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "*.txt")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler exit(){
		def eventHandler = new EventHandler(name: "exit")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'output.txt', appendNewLine: true, writingMode: WritingMode.APPEND)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
}
