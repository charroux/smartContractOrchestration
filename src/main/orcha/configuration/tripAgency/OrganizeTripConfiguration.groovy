package configuration.tripAgency

import groovy.util.logging.Slf4j
import orcha.lang.configuration.*
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import service.tripAgency.HotelSelection
import service.tripAgency.TaxiSelection
import service.tripAgency.TrainSelection
import service.tripAgency.TrainSelection

@Configuration
@Slf4j
class OrganizeTripConfiguration {

	@Bean
	EventHandler tripAgency(){
		def eventHandler = new EventHandler(name: "tripAgency")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "tripInfo.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.util.List", adapter: fileAdapter)
		return eventHandler
	}
	
	/*@Bean
	Application selectTrain(){
		def program = new Application(name: "selectTrain", specifications: "Select a train.", description: "Select a train.", language: "groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.tripAgency.TrainSelection', method:'select')
		program.input = new Input(type: "configuration.tripAgency.TripInfo", adapter: javaAdapter)
		program.output = new Output(type: "service.tripAgency.SelectedTrain", adapter: javaAdapter)
		return program
	}*/

	@Bean
	Application selectTrain(){
		def program = new Application(name: "selectTrain", specifications: "Select a train.", description: "Select a train.", language: "groovy")
		def blockchainAdapter = new BlockchainAdapter(javaClass: 'service.tripAgency.TrainSelection', method:'select')
		program.input = new Input(type: "configuration.tripAgency.TripInfo", adapter: blockchainAdapter)
		program.output = new Output(type: "service.tripAgency.SelectedTrain", adapter: blockchainAdapter)
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
		program.input = new Input(type: "service.tripAgency.SelectedTrain", adapter: javaAdapter)
		program.output = new Output(type: "service.tripAgency.SelectedHotel", adapter: javaAdapter)
		return program
	}
	
	@Bean
	HotelSelection hotelSelection(){
		return new HotelSelection()
	}

	@Bean
	Application selectTaxi(){
		def program = new Application(name: "selectTaxi", specifications: "Select a taxi.", description: "Select a taxi.", language: "groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.tripAgency.TaxiSelection', method:'select')
		program.input = new Input(type: "java.util.List", adapter: javaAdapter)
		program.output = new Output(type: "service.tripAgency.SelectedTaxi", adapter: javaAdapter)
		return program
	}
	
	@Bean
	TaxiSelection taxiSelection(){
		return new TaxiSelection()
	}

	@Bean
	EventHandler tripAgencyCustomer(){
		def eventHandler = new EventHandler(name: "tripAgencyCustomer")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'trip.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.tripAgency.SelectedTaxi", adapter: fileAdapter)
		return eventHandler
	}
	
}
