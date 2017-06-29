package orcha.lang.configuration

import org.springframework.context.annotation.Bean;

import groovy.transform.ToString
import groovy.transform.AutoClone
import groovy.transform.EqualsAndHashCode

import orcha.lang.configuration.ConfigurableProperties

/**
 *	Configuration of a service called by compute. For example<br>
 *	compute orderConverter<br>
 *	can be configured with:<br>
 *  &#064;Bean<br>
	Application orderConverter(){<br>
		def program = new Application(name: "orderConverter", language: "Groovy")<br>
		def javaAdapter = new JavaServiceAdapter(javaClass: 'VendorOrderConverter', method:'convert')<br>
		program.input = new Input(type: "service.order.Order", adapter: javaAdapter)<br>
		program.output = new Output(type: "service.order.SpecificOrder", adapter: javaAdapter)<br>
		return program<br>
	}<br><br>
	
	where VendorOrderConverter is a Groovy class configured as follow:<br>
	
	&#064;Bean<br>
	VendorOrderConverter vendorOrderConverter(){<br>
		return new VendorOrderConverter()<br>
	}<br><br>
	
	The VendorOrderConverter class is defined as:<br>
	
	package service.order<br>
	class VendorOrderConverter {<br>
	SpecificOrder convert(Order order){<br>
		// ...<br>
	}<br><br>
	
	In the case of a Javascript service call, for instance:<br>
	compute vendorComparison<br>
	&#064;Bean<br>
	Application vendorComparison(){<br>
		def jsApp = new Application(name: "vendorComparison", language: "js")<br>
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:orcha/service/order/vendorComparison.js')<br>
		jsApp.input = new Input(type: "service.order.Order", adapter: scriptAdapter)<br>
		jsApp.output = new Output(type: "service.order.Bill", adapter: scriptAdapter)<br>
		return jsApp<br>
	}<br><br>
	
	vendorComparison.js is defined as:<br>
	function vendorComparison(order) {<br>
	var billJSon = '{ "date":"", "price":1100 }';<br>
	var bill = JSON.parse(billJSon);<br>
	bill.date = new Date().getTime();<br>
	return bill;<br>
	}<br>
	
	vendorComparison(payload);<br><br>
	
	

	
 * @Since 0.1	
 * @author Ben C.
 *
 */
@ToString
@AutoClone
@EqualsAndHashCode
public class Application {

	/**
	 * State of a service.<br>
	 * When the Orcha programmer write expressions like:<br>
	 * when "application terminates" or when "application fails"<br>
	 * contains the state of the service.
	 * @Since 0.1
	 * @author Ben C.
	 *
	 */
	
	String specifications
	String name
	String description
	
	/**
	 * Groovy, Java, js...
	 */
	String language	
	
	def properties = [:]
	
	/**
	 * The input event of a service called by a compute instruction, there is a single event but it can contains many messages. 
	 */
	Input input
	
	/**
	 * The output event of a service called by a compute instruction, there is a single event but it can contains many messages.
	 */
	Output output	

	/**
	 * Application state: should not be set programmatically (managed at runtime).
	 */
	State state
	
	Error error
}

public enum State{
	TERMINATED,
	FAILED,
	RUNNING
}

/**
 * Configuration of the event received as an argument of a service call.<br>
 * @Since 0.1
 * @author Ben  C.
 *
 */
@ToString
@EqualsAndHashCode
public class Input{
	/**
	 * MIME type like application/json.
	 */
	String mimeType	//
	/**
	 * java.lang.Integer for instance. Primitive types like int forbidden.
	 */
	String type
	/**
	 * Content of the input event often in Json format, should not be set programmatically.
	 */
	def value
	/**
	 * Any Orcha adapter like {@link orcha.lang.configuration.JavaServiceAdapter}
	 */
	def adapter
	
	boolean autoStartup = true
}

/**
 * Configuration of the event returned by a service call.<br>
 * @Since 0.1
 * @author Charroux_std
 *
 */
@ToString
@EqualsAndHashCode
public class Output{
	/**
	 * MIME type like application/json.
	 */
	String mimeType
	/**
	 * java.lang.Integer for instance. Primitive types like int forbidden.
	 */
	String type
	/**
	 * Content of the output event often in Json format, should not be set programmatically.
	 */
	def value
	/**
	 * Any Orcha adapter like {@link orcha.lang.configuration.JavaServiceAdapter}
	 */
	ConfigurableProperties adapter
	
	boolean autoStartup = true
}







