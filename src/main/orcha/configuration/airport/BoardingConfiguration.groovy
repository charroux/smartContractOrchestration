package configuration.airport

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import service.airport.AlertService
import service.airport.ControlPassengerIdentity
import service.airport.LuggageScanning
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@Configuration
class BoardingConfiguration {
	
	@Bean
	EventHandler passenger(){
		def eventHandler = new EventHandler(name: "passenger")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "passenger.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.airport.Person", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler airportHandling(){
		def eventHandler = new EventHandler(name: "airportHandling")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "luggage.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.airport.Luggage", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	ControlPassengerIdentity controlPassengerIdentity() {
		return new ControlPassengerIdentity()
	}
	
	@Bean
	Application controlIdentity(){
		def application = new Application(name: "controlIdentity", language: "Java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.airport.ControlPassengerIdentity', method:'control')
		application.input = new Input(type: "service.airport.Person", adapter: javaAdapter)
		application.output = new Output(type: "service.airport.Passenger", adapter: javaAdapter)
		return application
	}
	
	@Bean
	LuggageScanning luggageScanning() {
		return new LuggageScanning()
	}
	
	@Bean
	Application scanLuggage(){
		def application = new Application(name: "scanLuggage", language: "Java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.airport.LuggageScanning', method:'scan')
		application.input = new Input(type: "service.airport.Luggage", adapter: javaAdapter)
		application.output = new Output(type: "service.airport.LuggageScan", adapter: javaAdapter)
		return application
	}

	@Bean
	AlertService alertService() {
		return new AlertService()
	}
	
	@Bean
	Application alertAuthorities(){
		def application = new Application(name: "alertAuthorities", language: "Java")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.airport.AlertService', method:'alert')
		application.input = new Input(type: "service.airport.LuggageScan", adapter: javaAdapter)
		application.output = new Output(type: "service.airport.SecurityAlert", adapter: javaAdapter)
		return application
	}
	
	@Bean
	EventHandler diplomaticService(){
		def eventHandler = new EventHandler(name: "diplomaticService")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'securityAlert.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.airport.SecurityAlert", adapter: fileAdapter)
		return eventHandler
	}

}
