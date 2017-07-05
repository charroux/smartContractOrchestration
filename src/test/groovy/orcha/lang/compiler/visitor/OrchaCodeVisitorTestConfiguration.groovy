package orcha.lang.compiler.visitor

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.referenceimpl.ExpressionParserImpl
import orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator
import orcha.lang.compiler.serviceOffer.ServiceOfferSelectionGenerator
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.impl.OrchaCodeParserImpl
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@SpringBootConfiguration
class OrchaCodeVisitorTestConfiguration {

	@Bean
	OrchaCodeParser orchaCodeParser(){
		return new OrchaCodeParserImpl()
	}
	
	@Bean
	ExpressionParser ExpressionParser(){
		return new ExpressionParserImpl()
	}
	
	@Bean
	EventHandler simpleApplicationInput(){
		def eventHandler = new EventHandler(name: "simpleApplicationInput")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "simpleApplicationInput.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "boolean", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application simpleApplicationService(){
		def jsApp = new Application(name: "simpleApplicationService", language: "js")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/simpleTest/simpleApplicationService.js')
		jsApp.input = new Input(type: "boolean", adapter: scriptAdapter)
		jsApp.output = new Output(type: "boolean", adapter: scriptAdapter)
		return jsApp
	}
	
	@Bean
	EventHandler simpleApplicationOutput(){
		def eventHandler = new EventHandler(name: "simpleApplicationOutput")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'simpleApplicationOutput.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "boolean", adapter: fileAdapter)
		return eventHandler
	}
	
	
	
	
	
	@Bean
	EventHandler orchaFile(){
		def eventHandler = new EventHandler(name: "orchaFile")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "SimpleApplication.groovy")
		eventHandler.input = new Input(mimeType: "text/plain", type: "String", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application parseOrcha(){
		def program = new Application(name: "parseOrcha", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'orcha.lang.compiler.visitor.OrchaCodeParser', method:'parse')
		program.input = new Input(type: "java.lan.String", adapter: javaAdapter)
		program.output = new Output(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		return program
	}
	
	@Bean
	Application generateServiceOfferSelection(){
		def program = new Application(name: "generateServiceOfferSelection", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'orcha.lang.compiler.serviceOffer.ServiceOfferSelectionGenerator', method:'generate')
		program.input = new Input(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		program.output = new Output(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		return program
	}
	
	@Bean
	ServiceOfferSelectionGenerator serviceOfferSelectionGenerator(){
		return new ServiceOfferSelectionGenerator()
	}
	
	@Bean
	Application generateMockOfServices(){
		def program = new Application(name: "generateMockOfServices", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator', method:'generate')
		program.input = new Input(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		program.output = new Output(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		return program
	}
	
	@Bean
	ConfigurationMockGenerator configurationMockGenerator(){
		return new ConfigurationMockGenerator()
	}
	
}
