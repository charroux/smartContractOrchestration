package configuration.qos

import orcha.lang.configuration.OutputFileAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import service.qos.Service1

import orcha.lang.configuration.OutputFileAdapter.WritingMode

/**
 * 
 * The circuit is opened after two failure.
 * If another service call is done before 5000 ms (delay to return to the state half-opening state) then the service is not called anymore (state opened).
 * 
 * @author Ben C.
 *
 */
@CircuitBreaker(numberOfFailuresBeforeOpening=2, intervalBeforeHalfOpening=5000L)
class ServiceCircuitBreakerConfiguration extends Application{
}

@Configuration
class CircuitBreakerConfiguration extends QoSConfiguration {

	@Bean
	Service1 service1(){
		return new Service1()
	}

	@Bean
	Application serviceWithCircuitBreaker(){
		def program = new ServiceCircuitBreakerConfiguration(name: "serviceWithCircuitBreaker", language: "Java", description: "The 3 files whose name starts with circuitBreakerInputFile are readen, so, the service is called 3 times. After 2 exceptions, the circuit breaker is opened. Then, before it is half-opened, a try of a service call fails.")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.qos.Service1', method:'myMethod')
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
	EventHandler qosOutputFile(){
		def eventHandler = new EventHandler(name: "qosOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'qosOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}

}
