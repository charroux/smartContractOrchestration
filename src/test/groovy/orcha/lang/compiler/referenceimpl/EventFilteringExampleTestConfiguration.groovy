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
import orcha.lang.configuration.ScriptServiceAdapter
import service.GroovyEventFilteringCodeTest

@Configuration
trait EventFilteringExampleTestConfiguration {
	
	@Bean
	EventHandler eventFilteringInputFile(){
		def eventHandler = new EventHandler(name: "eventFilteringInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "eventFilteringInputFile.txt")
		eventHandler.input = new Input(mimeType: "text/plain", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	GroovyEventFilteringCodeTest groovyEventFilteringCode(){
		return new GroovyEventFilteringCodeTest()
	}
	
	@Bean
	Application eventFilteringCode(){
		def codeApplication = new Application(name: "eventFilteringCode", language: "Groovy", description:"Receives and integer and returns the opposite value")
		def groovyCodeAdapter = new JavaServiceAdapter(javaClass: 'service.GroovyEventFilteringCodeTest', method:'method')
		def codeInput = new Input(type: "java.lang.Integer", adapter: groovyCodeAdapter)
		codeApplication.input = codeInput
		def codeOutput = new Output(type: "java.lang.Integer", adapter: groovyCodeAdapter)
		codeApplication.output = codeOutput
		return codeApplication
	}
	
	@Bean
	EventHandler eventFilteringOutputFile(){
		def eventHandler = new EventHandler(name: "eventFilteringOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'eventFilteringOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
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
