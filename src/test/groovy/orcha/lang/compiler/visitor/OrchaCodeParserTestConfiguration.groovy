package orcha.lang.compiler.visitor

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.referenceimpl.ExpressionParserImpl
import orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator
import orcha.lang.compiler.serviceOffer.ServiceOfferSelectionGenerator
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.impl.OrchaCodeParserImpl
import orcha.lang.configuration.Application
import orcha.lang.configuration.BranchPoint
import orcha.lang.configuration.BranchingPosition
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.MessagingMiddlewareAdapter
import orcha.lang.configuration.OrchaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.Queue
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.EventSourcing.JoinPoint
import orcha.lang.configuration.EventSourcing.MessageStore
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import service.orchaPartitioning.OrchaBankingService2
import service.order.VendorComparison
import service.order.VendorOrderConverter
import service.travelAgency.HotelSelection
import service.travelAgency.TaxiSelection
import service.travelAgency.TrainSelection

@SpringBootConfiguration
class OrchaCodeParserTestConfiguration {

	@Bean
	OrchaCodeParser orchaCodeParser(){
		return new OrchaCodeParserImpl()
	}
	
	@Bean
	ExpressionParser ExpressionParser(){
		return new ExpressionParserImpl()
	}
	
	
	
	@Bean
	EventHandler simpleApplicationInput(){
		def eventHandler = new EventHandler(name: "simpleApplicationInput")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "simpleApplicationInput.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "boolean", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application simpleApplicationService(){
		def jsApp = new Application(name: "simpleApplicationService", language: "js")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/simpleTest/simpleApplicationService.js')
		jsApp.input = new Input(type: "boolean", adapter: scriptAdapter)
		jsApp.output = new Output(type: "boolean", adapter: scriptAdapter)
		return jsApp
	}
	
	@Bean
	EventHandler simpleApplicationOutput(){
		def eventHandler = new EventHandler(name: "simpleApplicationOutput")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'simpleApplicationOutput.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "boolean", adapter: fileAdapter)
		return eventHandler
	}
	
	
	
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
	Application generateServiceOfferSelection(){
		def program = new Application(name: "generateServiceOfferSelection", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'orcha.lang.compiler.serviceOffer.ServiceOfferSelectionGenerator', method:'generate')
		program.input = new Input(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		program.output = new Output(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		return program
	}
	
	@Bean
	ServiceOfferSelectionGenerator serviceOfferSelectionGenerator(){
		return new ServiceOfferSelectionGenerator()
	}
	
	@Bean
	Application generateMockOfServices(){
		def program = new Application(name: "generateMockOfServices", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator', method:'generate')
		program.input = new Input(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		program.output = new Output(type: "orcha.lang.compiler.visitor.OrchaCodeVisitor", adapter: javaAdapter)
		return program
	}
	
	@Bean
	ConfigurationMockGenerator configurationMockGenerator(){
		return new ConfigurationMockGenerator()
	}
	
	
	
	
	@Bean
	EventHandler customer(){
		EventHandler eventHandler = new EventHandler(name: "customer")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "orderTV.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.order.Order", adapter: fileAdapter)
		//eventHandler.input = new Input(mimeType: "application/json", type: "service.order.Order")
		return eventHandler
	}
	
	@Bean
	Application orderConverter(){
		def program = new Application(name: "orderConverter", specifications: "bla bla", description: "bla bla")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.order.VendorOrderConverter', method:'convert')
		program.input = new Input(type: "service.order.Order", adapter: javaAdapter)
		program.output = new Output(type: "service.order.SpecificOrder", adapter: javaAdapter)
		//program.input = new Input(type: "service.order.Order")
		//program.output = new Output(type: "service.order.SpecificOrder")
		return program
	}
	
	@Bean
	VendorOrderConverter vendorOrderConverter(){
		return new VendorOrderConverter()
	}
	
	@Bean
	Application vendor1(){
		def jsApp = new Application(name: "vendor1", language: "js")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/order/vendor1.js')
		jsApp.input = new Input(type: "service.order.SpecificOrder", adapter: scriptAdapter)
		jsApp.output = new Output(type: "service.order.Bill", adapter: scriptAdapter)
		return jsApp
	}
	
	@Bean
	Application vendor2(){
		def jsApp = new Application(name: "vendor2", language: "js")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/order/vendor2.js')
		jsApp.input = new Input(type: "service.order.Order", adapter: scriptAdapter)
		jsApp.output = new Output(type: "service.order.Bill", adapter: scriptAdapter)
		return jsApp
	}

	@Bean
	Application vendor3(){
		def jsApp = new Application(name: "vendor3", language: "js")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/order/vendor3.js')
		jsApp.input = new Input(type: "service.order.Order", adapter: scriptAdapter)
		jsApp.output = new Output(type: "service.order.Bill", adapter: scriptAdapter)
		return jsApp
	}

	@Bean
	Application selectBestVendor(){
		def program = new Application(name: "selectBestVendor", language: "Java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.order.VendorComparison', method:'compare')
		program.input = new Input(type: "java.util.List<service.order.Bill>", adapter: javaAdapter)
		program.output = new Output(type: "service.order.Bill", adapter: javaAdapter)
		return program
	}
	
	@Bean
	VendorComparison vendorComparison(){
		return new VendorComparison()
	}
		
	@Bean
	EventHandler outputFile1(){
		def eventHandler = new EventHandler(name: "outputFile1")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'output1.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill", adapter: fileAdapter)
		//eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill")
		return eventHandler
	}
	
	@Bean
	EventHandler outputFile2(){
		def eventHandler = new EventHandler(name: "outputFile2")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'output2.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler travelAgency(){
		def eventHandler = new EventHandler(name: "travelAgency")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "travelInfo.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.util.List", adapter: fileAdapter)
		return eventHandler
	}

	@Bean
	Application selectTrain(){
		def program = new Application(name: "selectTrain", specifications: "Select a train.", description: "Select a train.", language: "groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.travelAgency.TrainSelection', method:'select')
		program.input = new Input(type: "configuration.travelAgency.TravelInfo", adapter: javaAdapter)
		program.output = new Output(type: "service.travelAgency.SelectedTrain", adapter: javaAdapter)
		return program
	}
	
	@Bean
	TrainSelection trainSelection(){
		return new TrainSelection()
	}
	
	@Bean
	Application selectHotel(){
		def program = new Application(name: "selectHotel", specifications: "Select a hotel.", description: "Select a hotel.", language: "groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.travelAgency.HotelSelection', method:'select')
		program.input = new Input(type: "service.travelAgency.SelectedTrain", adapter: javaAdapter)
		program.output = new Output(type: "service.travelAgency.SelectedHotel", adapter: javaAdapter)
		return program
	}
	
	@Bean
	HotelSelection hotelSelection(){
		return new HotelSelection()
	}

	@Bean
	Application selectTaxi(){
		def program = new Application(name: "selectTaxi", specifications: "Select a taxi.", description: "Select a taxi.", language: "groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.travelAgency.TaxiSelection', method:'select')
		program.input = new Input(type: "java.util.List", adapter: javaAdapter)
		program.output = new Output(type: "service.travelAgency.SelectedTaxi", adapter: javaAdapter)
		return program
	}
	
	@Bean
	TaxiSelection taxiSelection(){
		return new TaxiSelection()
	}

	@Bean
	EventHandler travelAgencyCustomer(){
		def eventHandler = new EventHandler(name: "travelAgencyCustomer")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'trip.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.travelAgency.SelectedTaxi", adapter: fileAdapter)
		return eventHandler
	}

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

}
