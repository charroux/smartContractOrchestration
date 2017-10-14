package configuration.qos

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.Queue
import service.qos.EnQueuedService
import service.qos.Service
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@Queue(capacity=20L, fixedDelay=500L)
class InputEventHandlerWithQueue extends EventHandler{
}

@Queue(capacity=20L, fixedDelay=1000L)
class ServiceWithQueueConfiguration extends Application{
}

@Queue(capacity=20L, fixedDelay=3000L)
class OutputEventHandlerWithQueue extends EventHandler{
}

@Configuration
class QueueConfiguration {
	
	@Bean
	EnQueuedService enQueuedService(){
		return new EnQueuedService()
	}
	
	@Bean
	Application serviceWithQueue(){
		def program = new ServiceWithQueueConfiguration(name: "serviceWithQueue", language: "Java", description: "")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.qos.EnQueuedService', method:'myMethod')
		program.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program
	}
	
	@Bean
	EventHandler queueInputFile(){
		def eventHandler = new InputEventHandlerWithQueue(name: "queueInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "queueInputFile*.txt")
		eventHandler.input = new Input(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler queueOutputFile(){
		def eventHandler = new OutputEventHandlerWithQueue(name: "queueOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'queueOutputFile.txt', appendNewLine: true, writingMode: WritingMode.APPEND)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}

}
