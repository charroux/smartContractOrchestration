package orcha.lang.compiler.referenceimpl

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.MessagingMiddlewareAdapter
import orcha.lang.configuration.OrchaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import service.orchaPartitioning.OrchaBankingService2
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@Configuration
trait OrchaServiceJavaServiceFilteringTestConfiguration {
	
	@Bean
	EventHandler bankCustomer(){
		EventHandler eventHandler = new EventHandler(name: "bankCustomer")
		def inputFileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "bankOrder.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.orchaPartitioning.Order", adapter: inputFileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler bankCustomer1(){
		EventHandler eventHandler = new EventHandler(name: "bankCustomer1")
		def outputFileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'bankingTransaction.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.orchaPartitioning.BankingTransaction", adapter: outputFileAdapter)
		return eventHandler
	}
	
	@Bean
	Application processOrderBank1(){
		
		def application = new Application(name: "processOrderBank1", language: "Orcha")
		
		def orchaServiceAdapter = new OrchaServiceAdapter()
		
		application.input = new Input(type: "service.orchaPartitioning.Order", adapter: orchaServiceAdapter)
		application.output = new Output(type: "service.orchaPartitioning.BankingTransaction", adapter: orchaServiceAdapter)
			
		def inputEventHandler = new EventHandler(name: "bankingTransaction")
		inputEventHandler.input = new Input(mimeType: "application/json", type: "service.orchaPartitioning.BankingTransaction", adapter: new MessagingMiddlewareAdapter())
		orchaServiceAdapter.inputEventHandler = inputEventHandler
		
		def outputEventHandler = new EventHandler(name: "bankingOrder")
		outputEventHandler.output = new Output(mimeType: "application/json", type: "service.orchaPartitioning.Order", adapter: new MessagingMiddlewareAdapter())
		orchaServiceAdapter.outputEventHandler = outputEventHandler
		
		return application
	}

	@Bean
	OrchaBankingService2 orchaBankingService2(){
		return new OrchaBankingService2()
	}
	
	@Bean
	Application processOrderBank2(){
		def application = new Application(name: "processOrderBank2", language: "Groovy")
		def adapter = new JavaServiceAdapter(javaClass: 'service.orchaPartitioning.OrchaBankingService2', method:'process')
		def codeInput = new Input(type: "service.orchaPartitioning.Order", adapter: adapter)
		application.input = codeInput
		def codeOutput = new Output(type: "service.orchaPartitioning.BankingTransaction", adapter: adapter)
		application.output = codeOutput
		return application
	}


	
	@Bean
	Application processOrderBank3(){
		
		def application = new Application(name: "processOrderBank3", language: "Orcha")
		
		def orchaServiceAdapter = new OrchaServiceAdapter()
		
		application.input = new Input(type: "service.orchaPartitioning.Order", adapter: orchaServiceAdapter)
		application.output = new Output(type: "service.orchaPartitioning.BankingTransaction", adapter: orchaServiceAdapter)
			
		def inputEventHandler = new EventHandler(name: "bankingTransaction")
		inputEventHandler.input = new Input(mimeType: "application/json", type: "service.orchaPartitioning.BankingTransaction", adapter: new MessagingMiddlewareAdapter())
		orchaServiceAdapter.inputEventHandler = inputEventHandler
		
		def outputEventHandler = new EventHandler(name: "bankingOrder")
		outputEventHandler.output = new Output(mimeType: "application/json", type: "service.orchaPartitioning.Order", adapter: new MessagingMiddlewareAdapter())
		orchaServiceAdapter.outputEventHandler = outputEventHandler
		
		return application
	}

}
