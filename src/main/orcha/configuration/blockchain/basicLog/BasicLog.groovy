package configuration.blockchain.basicLog

import orcha.lang.configuration.Application;
import orcha.lang.configuration.EventHandler;
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.EventSourcing.MessageStore

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import service.blockchain.basicLog.Service4Class
import service.order.VendorOrderConverter;

@EventSourcing(messageStore=MessageStore.mongoDB, eventName="")
class PersistentService extends Application{
}

@EventSourcing(messageStore=MessageStore.mongoDB, eventName="")
class PersistentSend extends EventHandler{
}

@Configuration
class BasicLog {
	
	@Bean
	EventHandler basicLogInputFile(){
		def eventHandler = new EventHandler(name: "basicLogInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/blockchain', filenamePattern: "data.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.blockchain.basicLog.Person", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application service4(){	
		def program = new PersistentService(name: "service4", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'Service4Class', method:'method')
		program.input = new Input(type: "service.blockchain.basicLog.Person", adapter: javaAdapter)
		program.output = new Output(type: "service.blockchain.basicLog.Person", adapter: javaAdapter)
		return program
	}
	
	@Bean
	Service4Class service4Class(){
		return new Service4Class()
	}
	
	
	@Bean
	EventHandler basicLogOutputFile(){
		def eventHandler = new PersistentSend(name: "basicLogOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/blockchain', createDirectory: true, filename:'output1.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.blockchain.basicLog.Person", adapter: fileAdapter)
		return eventHandler
	}

}
