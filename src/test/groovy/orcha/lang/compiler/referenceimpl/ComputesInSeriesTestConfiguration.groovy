package orcha.lang.compiler.referenceimpl

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.compiler.Compile
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.referenceimpl.xmlgenerator.XmlGenerator
import orcha.lang.compiler.visitor.OrchaCodeParser
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
import service.Program1
import service.Program2
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.JDom2XmlGeneratorForSpringIntegration
import orcha.lang.compiler.visitor.impl.OrchaCodeParserImpl
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.Retry
import service.prepareOrder.OrderPreparation

// Test configurations
trait ComputesInSeriesTestConfiguration {

	@Bean
	EventHandler computesInSeriesInputFile(){
		def eventHandler = new EventHandler(name: "computesInSeriesInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "computesInSeriesInputFile.txt")
		eventHandler.input = new Input(mimeType: "text/plain", type: "java.lang.Integer", adapter: fileAdapter)
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
	EventHandler computesInSeriesOutputFile(){
		def eventHandler = new EventHandler(name: "computesInSeriesOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'computesInSeriesOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	OrchaCodeParser orchaCodeParser(){
		return new OrchaCodeParserImpl()
	}

	@Bean
	Compile compile(){
		return new CompileServiceWithSpringIntegration()
	}
	
	@Bean
	ExpressionParser ExpressionParser(){
		return new ExpressionParserImpl()
	}
	
	@Bean
	QualityOfService qualityOfService(){
		return new QualityOfServiceImpl()
	}
	
	@Bean
	XmlGenerator xmlGenerator(){
		return new JDom2XmlGeneratorForSpringIntegration()
	}
	
	@Bean
	OrchaLauncherGenerator orchaLauncherGenerator(){
		return new OrchaLauncherGenerator()
	}

}
