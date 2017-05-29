package configuration.tacitContract

import orcha.lang.configuration.Application;
import orcha.lang.configuration.EventHandler;
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import service.order.VendorOrderConverter;
import service.tacitContract.service.CarInsuranceValidity;
import service.tacitContract.service.DrivingLicenceValidity;
import service.tacitContract.service.TaxiValidity;

@Configuration
class TacitContractConfiguration {
	
	@Bean
	EventHandler drivingLicenceRepository(){
		def eventHandler = new EventHandler(name: "drivingLicenceRepository")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/inputTacitContact', filenamePattern: "drivingLicense.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.tacitContract.domain.DrivingLicence", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler carInsuranceRepository(){
		def eventHandler = new EventHandler(name: "carInsuranceRepository")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/inputTacitContact', filenamePattern: "carInsurance.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.tacitContract.domain.CarInsurance", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application checkDrivingLicenceValidity(){
		def program = new Application(name: "checkDrivingLicenceValidity", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.tacitContract.service.DrivingLicenceValidity', method:'check')
		program.input = new Input(type: "service.tacitContract.domain.DrivingLicence", adapter: javaAdapter)
		program.output = new Output(type: "service.tacitContract.domain.AvailableDrivingLicence", adapter: javaAdapter)
		return program
	}
	
	@Bean
	DrivingLicenceValidity drivingLicenceValidity(){
		return new DrivingLicenceValidity()
	}
	
	@Bean
	Application checkCarInsuranceValidity(){
		def program = new Application(name: "checkCarInsuranceValidity", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.tacitContract.service.CarInsuranceValidity', method:'check')
		program.input = new Input(type: "service.tacitContract.domain.CarInsurance", adapter: javaAdapter)
		program.output = new Output(type: "service.tacitContract.domain.AvailableCarInsurance", adapter: javaAdapter)
		return program
	}
	
	@Bean
	CarInsuranceValidity carInsuranceValidity(){
		return new CarInsuranceValidity()
	}
	
	@Bean
	Application authorizeTaxi(){
		def program = new Application(name: "authorizeTaxi", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.tacitContract.service.TaxiValidity', method:'authorize')
		program.input = new Input(type: "service.tacitContract.domain.AvailableDrivingLicence", adapter: javaAdapter)
		program.output = new Output(type: "service.tacitContract.domain.Taxi", adapter: javaAdapter)
		return program
	}
	
	@Bean
	TaxiValidity taxiValidity(){
		return new TaxiValidity()
	}
	
	@Bean
	EventHandler prefecture(){
		def eventHandler = new EventHandler(name: "prefecture")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/outputTacitContact', createDirectory: true, filename:'output1.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.tacitContract.domain.Taxi", adapter: fileAdapter)
		return eventHandler
	}

}
