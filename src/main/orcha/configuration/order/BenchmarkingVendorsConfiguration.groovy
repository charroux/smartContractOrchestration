package configuration.order

import groovy.transform.ToString;
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.Queue
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.EventSourcing.MessageStore
import orcha.lang.configuration.EventSourcing.JoinPoint
import orcha.lang.configuration.BranchPoint
import orcha.lang.configuration.BranchingPosition
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.Retry
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

import org.springframework.beans.PropertyEditorRegistrar
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.CustomEditorConfigurer
import org.springframework.beans.propertyeditors.CustomDateEditor
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import service.order.Order
import service.order.Product
import service.order.SpecificOrder
import service.order.VendorComparison
import service.order.VendorOrderConverter

/*

//@EventSourcing(messageStore=MessageStore.mongoDB, eventName="")
//@Queue(capacity=30L, fixedRate=1000L)
//@Retry(maxNumberOfAttempts=3, intervalBetweenTheFirstAndSecondAttempt=5000L, intervalMultiplierBetwennAttemps=2, maximumIntervalBetweenAttempts=20000L)
@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.beforeAndAfter, eventName="")
class Vendor1Application extends Application{
}

@BranchPoint(configurationFile="dataConfig", position=BranchingPosition.after)
class SelectBestVendorApplication extends Application{
}*/

//@Queue(capacity=20L, fixedRate=100L)
//@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.after, eventName="")
class CustomerEventHandler extends EventHandler{
}

//@Queue(capacity=20L, fixedDelay=1000L)
@CircuitBreaker(numberOfFailuresBeforeOpening=2, intervalBeforeHalfOpening=2000L)
@Retry(maxNumberOfAttempts=3, intervalBetweenTheFirstAndSecondAttempt=5000L, intervalMultiplierBetweenAttemps=2, maximumIntervalBetweenAttempts=20000L)
//@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.after, eventName="log order conversion")
class OrderConverterApplication extends Application{
}

//@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.before, eventName="")
class OutputFile2EventHandler extends EventHandler{
}

@Configuration
@Slf4j
//@Profile("myBusinessProfile")
class BenchmarkingVendorsConfiguration {
	
/*	@Bean
	EventHandler customer(){
		def eventHandler = new CustomerEventHandler(name: "customer")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.order.Order")
		return eventHandler
	}
	
	@Bean
	Application orderConverter(){
		def program = new Application(name: "orderConverter", specifications: "Convert an order format into another one. Argument: service.order.Order. Return: service.order.SpecificOrder", description: "Convert a specific vendor order to a generic one.")
		program.input = new Input(type: "service.order.Order")
		program.output = new Output(type: "service.order.SpecificOrder")
		return program
	}
	
	@Bean
	Application vendor1(){
		def jsApp = new Application(name: "vendor1", language: "js", specifications: "Get a bill from the vendor from the given order. Argument: service.order.SpecificOrder. Return: service.order.Bill", description: "Get a bill from the vendor from the given order.")
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
		def program = new SelectBestVendorApplication(name: "selectBestVendor", language: "Java")
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
		eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill")
		return eventHandler
	}
	
	@Bean
	EventHandler outputFile2(){
		def eventHandler = new EventHandler(name: "outputFile2")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'output2.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill", adapter: fileAdapter)
		return eventHandler
	}*/
	
	@Bean
	EventHandler customer(){
		EventHandler eventHandler = new CustomerEventHandler(name: "customer")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "orderTV.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.order.Order", adapter: fileAdapter)
		//eventHandler.input = new Input(mimeType: "application/json", type: "service.order.Order")
		return eventHandler
	}
	
	@Bean
	Application orderConverter(){
		def program = new OrderConverterApplication(name: "orderConverter", specifications: "Convert an order format into another one. Argument: service.order.Order. Return: service.order.SpecificOrder", description: "Convert a specific vendor order into a generic one.")
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
		def jsApp = new Application(name: "vendor1", language: "js", specifications: "Get a bill from the vendor from the given order. Argument: service.order.SpecificOrder. Return: service.order.Bill", description: "Get a bill from the vendor from the given order.")
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
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'output1.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill", adapter: fileAdapter)
		//eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill")
		return eventHandler
	}
	
	@Bean
	EventHandler outputFile2(){
		def eventHandler = new OutputFile2EventHandler(name: "outputFile2")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'output2.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill", adapter: fileAdapter)
		return eventHandler
	}

}
