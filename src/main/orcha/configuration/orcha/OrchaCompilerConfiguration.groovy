package configuration.orcha

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.compiler.Compile
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.compiler.referenceimpl.CompileServiceWithSpringIntegration
import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.referenceimpl.ExpressionParserImpl
import orcha.lang.compiler.referenceimpl.OrchaLauncherGenerator
import orcha.lang.compiler.referenceimpl.configurationproperties.ConfigurationPropertiesGenerator
import orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator
import orcha.lang.compiler.referenceimpl.xmlgenerator.XmlGenerator
import orcha.lang.compiler.referenceimpl.xmlgenerator.connectors.SpringIntegrationConnectors
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.JDom2XmlGeneratorForSpringIntegration
import orcha.lang.compiler.serviceOffer.ServiceOfferSelectionGenerator
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.impl.OrchaCodeParserImpl
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output

@Configuration
class OrchaCompilerConfiguration {
	
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
	OrchaCodeParser orchaCodeParser1(){
		return new OrchaCodeParserImpl()
	}
	
	@Bean
	ExpressionParser ExpressionParser1(){
		return new ExpressionParserImpl()
	}
	
	@Bean
	Application generateServiceOfferSelection(){
		def program = new Application(name: "generateServiceOfferSelection", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'orcha.lang.compiler.serviceOffer.ServiceOfferSelectionGenerator', method:'generate')
		program.input = new Input(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		program.output = new Output(type: "boolean", adapter: javaAdapter)
		return program
	}
	
	@Bean
	ServiceOfferSelectionGenerator serviceOfferSelectionGenerator1(){
		return new ServiceOfferSelectionGenerator()
	}
	
	@Bean
	Application generateMockOfServices(){
		def program = new Application(name: "generateMockOfServices", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator', method:'generate')
		program.input = new Input(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		program.output = new Output(type: "boolean", adapter: javaAdapter)
		return program
	}
	
	@Bean
	ConfigurationMockGenerator configurationMockGenerator1(){
		return new ConfigurationMockGenerator()
	}
	
	@Bean
	Application compileForLaunching(){
		def program = new Application(name: "compileForLaunching", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'orcha.lang.compiler.Compile', method:'compileForLaunching')
		program.input = new Input(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		program.output = new Output(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		return program
	}
	
	@Bean
	ConfigurationPropertiesGenerator configurationPropertiesGenerator1(){
		return new ConfigurationPropertiesGenerator()
	}
	
	@Bean
	Compile compile1(){
		return new CompileServiceWithSpringIntegration()
	}
		
	@Bean
	XmlGenerator xmlGenerator1(){
		//return new XmlGeneratorForSpringIntegration()
		return new JDom2XmlGeneratorForSpringIntegration()
	}
	
	@Bean
	SpringIntegrationConnectors connectors(){
		return new SpringIntegrationConnectors()
	}
	
	@Bean
	QualityOfService qualityOfService1(){
		return new QualityOfServiceImpl()
	}
	
	@Bean
	OrchaLauncherGenerator orchaLauncherGenerator1(){
		return new OrchaLauncherGenerator()
	}

}
