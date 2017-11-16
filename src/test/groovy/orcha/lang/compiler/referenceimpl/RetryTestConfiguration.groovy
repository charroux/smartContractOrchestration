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
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.JDom2XmlGeneratorForSpringIntegration
import orcha.lang.compiler.visitor.impl.OrchaCodeParserImpl
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.Retry
import service.prepareOrder.OrderPreparation
import service.qos.Service

// Test configurations
trait RetryTestConfiguration{

	@Bean
	Application serviceWithRetry(){
		def program = new ServiceRetryConfiguration(name: "serviceWithRetry", language: "Java", description: "The service is retried 3 times. Two exceptions occurs during the two first attempts. Then the service completes at the thrid attempts.")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.qos.Service', method:'myMethod')
		program.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program
	}
	
	@Bean
	EventHandler retryInputFile(){
		def eventHandler = new EventHandler(name: "retryInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "retryInputFile.txt")
		eventHandler.input = new Input(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Service service(){
		return new Service()
	}
	
	@Bean
	EventHandler qosOutputFile(){
		def eventHandler = new EventHandler(name: "qosOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'qosOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
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

