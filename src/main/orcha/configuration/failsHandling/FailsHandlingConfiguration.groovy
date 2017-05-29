package configuration.failsHandling

import groovy.transform.ToString;

import java.text.SimpleDateFormat

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import orcha.lang.configuration.Queue
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.EventSourcing.MessageStore
import orcha.lang.configuration.BranchingPosition
import orcha.lang.configuration.ScriptServiceAdapter

import org.springframework.beans.PropertyEditorRegistrar
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.CustomEditorConfigurer
import org.springframework.beans.propertyeditors.CustomDateEditor
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Profile

import service.failsHandling.AClass
import service.failsHandling.AlternativeClass
import service.failsHandling.AnotherClass
import service.failsHandling.ErrorClass
import service.failsHandling.ServiceComparison
import service.order.Order
import service.order.VendorComparison;
import service.order.VendorOrderConverter

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect

@Configuration
class FailsHandlingConfiguration {
	
	@Bean
	EventHandler anInputFile(){
		def eventHandler = new EventHandler(name: "anInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "fails.txt")
		eventHandler.input = new Input(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application aService(){
		def program = new Application(name: "aService", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.failsHandling.AClass', method:'method')
		program.input = new Input(type: "java.lang.String", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.String", adapter: javaAdapter)
		return program
	}
	
	@Bean
	AClass aClass(){
		return new AClass()
	}
	
	@Bean
	Application anotherService(){
		def jsApp = new Application(name: "anotherService", language: "js")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:orcha/service/failsHandling/anotherService.js')
		jsApp.input = new Input(type: "java.lang.String", adapter: scriptAdapter)
		jsApp.output = new Output(type: "java.lang.String", adapter: scriptAdapter)
		return jsApp
	}
	
	/*@Bean
	Application anotherService(){
		def program = new Application(name: "anotherService", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'AnotherClass', method:'method')
		program.input = new Input(type: "java.lang.String", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.String", adapter: javaAdapter)
		return program
	}
	
	@Bean
	AnotherClass anotherClass(){
		return new AnotherClass()
	}*/
	
	@Bean
	Application finalService(){
		def program = new Application(name: "finalService", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.failsHandling.ServiceComparison', method:'compare')
		program.input = new Input(type: "java.util.List<java.lang.String>", adapter: javaAdapter)
		program.output = new Output(type: "java.util.List<java.lang.String>", adapter: javaAdapter)
		return program
	}
	
	@Bean
	ServiceComparison serviceComparison(){
		return new ServiceComparison()
	}
	
	
	@Bean
	Application alternativeService(){
		def program = new Application(name: "alternativeService", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.failsHandling.AlternativeClass', method:'method')
		program.input = new Input(type: "java.lang.String", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.String", adapter: javaAdapter)
		return program
	}
	
	@Bean
	AlternativeClass alternativeClass(){
		return new AlternativeClass()
	}

	@Bean
	ErrorClass errorClass(){
		return new ErrorClass()
	}
	
	@Bean
	EventHandler anOutputFile(){
		def eventHandler = new EventHandler(name: "anOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'anOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler anErrorFile(){
		def eventHandler = new EventHandler(name: "anErrorFile")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'anErrorFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}

}
