package configuration.travelAgency

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import service.travelAgency.HotelSelection
import service.travelAgency.TaxiSelection
import service.travelAgency.TrainSelection
import groovy.util.logging.Slf4j
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@Configuration
@Slf4j
class OrganizeTripConfiguration {

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
		def javaAdapter = new JavaServiceAdapter(javaClass: 'configuration.travelAgency.TravelInfo', method:'select')
		program.input = new Input(type: "java.util.List", adapter: javaAdapter)
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
	
}
