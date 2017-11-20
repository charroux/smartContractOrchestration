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
import service.prepareOrder.OrderPreparation

@CircuitBreaker(numberOfFailuresBeforeOpening=2, intervalBeforeHalfOpening=5000L)
class ServiceCircuitBreakerConfiguration extends Application{
}
// Test configurations
trait CircuitBreakerTestConfiguration {

	@Bean
	Application serviceWithCircuitBreaker(){
		def program = new ServiceCircuitBreakerConfiguration(name: "serviceWithCircuitBreaker", language: "Java", description: "The 3 files whose name starts with circuitBreakerInputFile are readen, so, the service is called 3 times. After 2 exceptions, the circuit breaker is opened. Then, before it is half-opened, a try of a service call fails.")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.qos.Service', method:'myMethod')
		program.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program
	}
	
	/**
	 * The filenamePattern is circuitBreakerInputFile*.txt. So all file starting with circuitBreakerInputFile are readen
	 * @return
	 */
	@Bean
	EventHandler circuitBreakerInputFile(){
		def eventHandler = new EventHandler(name: "circuitBreakerInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "circuitBreakerInputFile*.txt")
		eventHandler.input = new Input(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
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
