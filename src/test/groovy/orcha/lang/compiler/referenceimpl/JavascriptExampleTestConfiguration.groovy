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
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.JDom2XmlGeneratorForSpringIntegration
import orcha.lang.compiler.visitor.impl.OrchaCodeParserImpl
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.Retry
import orcha.lang.configuration.ScriptServiceAdapter
import service.prepareOrder.OrderPreparation

// Test configurations
trait JavascriptExampleTestConfiguration {

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
