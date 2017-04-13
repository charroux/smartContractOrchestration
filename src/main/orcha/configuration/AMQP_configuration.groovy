package configuration

import orcha.lang.configuration.AMQP_Adapter
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AMQP_configuration {
	
	@Bean
	EventHandler userInterface(){
		def eventHandler = new EventHandler(name: "userInterface")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "*.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.Order", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application anyLanguage(){
		def program = new Application(name: "anyLanguage")
		def amqpAdapter = new AMQP_Adapter(queueName: 'rpc_queue', host: 'localhost')
		program.output = new Output(mimeType: "application/json", type: "service.Order", adapter: amqpAdapter)
		program.input = new Input(mimeType: "application/json", type: "service.Order", adapter: amqpAdapter)
		return program
	}
	
	@Bean
	EventHandler productFile(){
		def eventHandler = new EventHandler(name: "productFile")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'productFile.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.Order", adapter: fileAdapter)
		return eventHandler
	}
}
