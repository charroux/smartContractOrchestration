package configuration.qos

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.Retry

/**
 * 
 * Interval between retries: 1000, 2000 = 1000 * 2, 4000 = 2000 * 2
 * 
 * @author Ben C.
 *
 */
@Retry(maxNumberOfAttempts=3, intervalBetweenTheFirstAndSecondAttempt=1000L, intervalMultiplierBetweenAttemps=2, maximumIntervalBetweenAttempts=4000L)
class ServiceRetryConfiguration extends Application{
}

@Configuration
class RetryConfiguration extends QoSConfiguration {
	
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

}
