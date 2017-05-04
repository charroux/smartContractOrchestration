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

/*import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect*/

/*class MyAdvice{
	
	@Autowired
	ApplicationContext context
	
	public void essai(Object retVal){
		println "kkkkkkkkkkkpppppppppppp: " + retVal + " " + context
		retVal.product = "Hifi"
		MyInterface myInterface = context.getBean("myInterface")
		myInterface.myMethod(retVal)
	}
	
}

interface MyInterface{
	void myMethod(Object object)	
}
*/

@Queue(capacity=20L, fixedRate=100L)
//@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.after, eventName="")
class CustomerEventHandler extends EventHandler{
}


//@Queue(capacity=20L, fixedDelay=1000L)
//@CircuitBreaker(numberOfFailuresBeforeOpening=2, intervalBeforeHalfOpening=2000L)
//@Retry(maxNumberOfAttempts=3, intervalBetweenTheFirstAndSecondAttempt=5000L, intervalMultiplierBetwennAttemps=2, maximumIntervalBetweenAttempts=20000L)
//@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.after, eventName="")
class OrderConverterApplication extends Application{
}

//@EventSourcing(messageStore=MessageStore.mongoDB, eventName="")
//@Queue(capacity=30L, fixedRate=1000L)
//@Retry(maxNumberOfAttempts=3, intervalBetweenTheFirstAndSecondAttempt=5000L, intervalMultiplierBetwennAttemps=2, maximumIntervalBetweenAttempts=20000L)
@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.beforeAndAfter, eventName="")
class Vendor1Application extends Application{
}

@BranchPoint(configurationFile="dataConfig", position=BranchingPosition.after)
class SelectBestVendorApplication extends Application{
}

/*@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.before, eventName="")
class OutputFile1EventHandler extends EventHandler{
}*/

//@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.before, eventName="")
/*class OutputFile2EventHandler extends EventHandler{
}*/

/**
 * This file defines.
 * It should be written by an operational.
 * See source/order for the configuration
 * @author Ben C.
 *
 */
@Configuration
@Slf4j
//@Profile("myBusinessProfile")
class BenchmarkingVendorsConfiguration {
	
	/**
	 * Defines a customer used in: receive order from customer condition "order.product.specification == 'TV'"
	 * customer is an EventHandler (a source for an event).
	 * The source of the event is a file (InputFileAdapter) defined by a directory (should match your system).
	 * The name of the fine is orderTV.json.
	 * It must be formatted according the MIME type json. The format is defined by Order (see service/order/Order.groovy)  
	 * @return
	 */
	@Bean
	@Scope("singleton")
	EventHandler customer(){
		def eventHandler = new CustomerEventHandler(name: "customer")
		//def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "orderTV.json")
		//eventHandler.input = new Input(mimeType: "application/json", type: "service.order.Order", adapter: fileAdapter)
		eventHandler.input = new Input(mimeType: "application/json", type: "service.order.Order")
		return eventHandler
	}
	
	/**
	 * Defines orderConverter used in: compute orderConverter with order.value
	 * orderConverter defines an application by its name (orderConverter), programming language (Groovy/Java), 
	 * the program launched (VendorOrderConverter), the function called in the program (convert)program, input and output.
	 * The program receives as input the type Order (see service/order/Order), 
	 * and returns the type SpecificOrder (see service/order/SpecificOrder)
	 * @return
	 */
/*	@Bean
	Application orderConverter(){
		def program = new OrderConverterApplication(name: "orderConverter", language: "Groovy")
		//def javaAdapter = new JavaServiceAdapter(javaClass: 'VendorOrderConverter', method:'convert')
		def javaAdapter = new JavaServiceAdapter(javaClass: 'VendorOrderConverterInterface', method:'convert')
		program.input = new Input(type: "service.order.Order", adapter: javaAdapter)
		program.output = new Output(type: "service.order.SpecificOrder", adapter: javaAdapter)
		return program
	}*/

	@Bean
	Application orderConverter(){
		def program = new OrderConverterApplication(name: "orderConverter", specifications: "bla bla", description: "bla bla")
		program.input = new Input(type: "service.order.Order")
		program.output = new Output(type: "service.order.SpecificOrder")
		return program
	}
	
/*	{
		"name": "orderConverter", 
		"specifications": "bla bla", 
		"description": "bla bla"),
		"input" : {
			"type": "service.order.Order"
		} 
		"output" : {
			"type": "service.order.Order"
		}
	}*/
	
	/**
	 * Defines the program to be launched by "compute orderConverter"
	 * See service/order/VendorOrderConverter.groovy
	 * @return
	 */
/*	@Bean
	VendorOrderConverter vendorOrderConverter(){
		return new VendorOrderConverter()
	}*/
	
	/*@Bean
	VendorOrderConverter vendorOrderConverter(){
		VendorOrderConverter vendorOrderConverter  = org.mockito.Mockito.spy(VendorOrderConverter.class)
		Product p = new Product();
		p.setSpecification("TV");
		Order order = new Order();
		order.setNumber(1);
		order.setProduct(p);
		SpecificOrder specificOrder = new SpecificOrder();
		specificOrder.setNumber(1111);
		specificOrder.setProduct("azerty");
		org.mockito.Mockito.when(vendorOrderConverter.convert(order)).thenReturn( specificOrder );
		return vendorOrderConverter
	}*/
	
/*	@Bean
	VendorOrderConverterInterface vendorOrderConverterInterface(){
		VendorOrderConverterInterface vendorOrderConverter  = org.mockito.Mockito.mock(VendorOrderConverterInterface.class)
		Product p = new Product(specification: "TV")
		Order order = new Order(number: 1, product: p);
		org.mockito.Mockito.when(vendorOrderConverter.convert(order)).thenReturn( new SpecificOrder(number: 1111, product: "azerty") );
		log.info "-------------------------------------------vendorOrderConverterInterface : " + vendorOrderConverter
		return vendorOrderConverter
	}*/
	
	/**
	 * Defines vendor1 used in: compute vendor1 with orderConverter.result
	 * The program launched is a Javascript program (see service/order/vendor1.js).
	 * It receives as input the type SpecificOrder (see service/order/SpecificOrder.groovy)
	 * and returns the type Bill (see service/order/Bill.groovy)
	 * @return
	 */
	@Bean
	Application vendor1(){
		def jsApp = new Vendor1Application(name: "vendor1", language: "js")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/order/vendor1.js')
		jsApp.input = new Input(type: "service.order.SpecificOrder", adapter: scriptAdapter)
		jsApp.output = new Output(type: "service.order.Bill", adapter: scriptAdapter)
		return jsApp
	}
	
	/**
	 * Defines vendor2 used in: compute vendor2 with order.value
	 * The program launched is a Javascript program (see service/order/vendor2.js)
	 * @return
	 */
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
		def javaAdapter = new JavaServiceAdapter(javaClass: 'VendorComparison', method:'compare')
		program.input = new Input(type: "java.util.List<service.order.Bill>", adapter: javaAdapter)
		program.output = new Output(type: "service.order.Bill", adapter: javaAdapter)
		return program
	}
	
	@Bean
	VendorComparison vendorComparison(){
		return new VendorComparison()
	}
		
	@Bean
	@Scope("singleton")
	EventHandler outputFile1(){
		//def eventHandler = new OutputFile1EventHandler(name: "outputFile1")
		def eventHandler = new EventHandler(name: "outputFile1")
		//def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'output1.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		//eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill", adapter: fileAdapter)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill")
		return eventHandler
	}
	
	@Bean
	EventHandler outputFile2(){
		//def eventHandler = new OutputFile2EventHandler(name: "outputFile2")
		def eventHandler = new EventHandler(name: "outputFile2")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'output2.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill", adapter: fileAdapter)
		//eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill")
		return eventHandler
	}
}
