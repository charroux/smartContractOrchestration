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
import service.GroovyCodeToBenchmark1Test
import service.GroovyCodeToBenchmark2Test
import service.GroovyCode1Test
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.JDom2XmlGeneratorForSpringIntegration
import orcha.lang.compiler.visitor.impl.OrchaCodeParserImpl
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.Retry
import service.prepareOrder.OrderPreparation

// Test configurations
trait BenchmarkingExampleTestConfiguration {

	@Bean
	EventHandler benchmarkingInputFile(){
		def eventHandler = new EventHandler(name: "benchmarkingInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "benchmarkingData.txt")
		eventHandler.input = new Input(mimeType: "text/plain", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	GroovyCodeToBenchmark1Test groovyCodeToBenchmark1(){
		return new GroovyCodeToBenchmark1Test()
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
	GroovyCodeToBenchmark2Test groovyCodeToBenchmark2(){
		return new GroovyCodeToBenchmark2Test()
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
