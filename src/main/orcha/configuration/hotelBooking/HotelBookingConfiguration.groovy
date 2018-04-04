package configuration.hotelBooking


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import groovy.util.logging.Slf4j
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.HttpAdapter
import orcha.lang.configuration.Input
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.EventSourcing.JoinPoint
import orcha.lang.configuration.EventSourcing.MessageStore
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import service.restWebService.Preparation


@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.after, eventName="")
class BookingDemandEventHandler extends EventHandler{
}


@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.after, eventName="")
class BookingApplication extends Application{
}

@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.before, eventName="")
class BookingReceiptEventHandler extends EventHandler{
}

@Configuration
@Slf4j
class HotelBookingConfiguration {
	
	/**
	 * JSon example: {"begin": "2018-07-28T22:25:51Z", "end": "2018-07-30T22:25:51Z", "numberOfBeds": 3}
	 * @return
	 */
	@Bean
	EventHandler onlineBookingDemand(){
		EventHandler eventHandler = new BookingDemandEventHandler(name: "onlineBookingDemand")
		def httpAdapter = new HttpAdapter(url: '/onlineBookingDemand', method: HttpAdapter.Method.POST)
		eventHandler.input = new Input(mimeType: "application/json", type: "service.hotelBooking.Booking", adapter: httpAdapter)
		return eventHandler
	}
	
	@Bean
	Application book(){
		def jsApp = new BookingApplication(name: "book", language: "js", specifications: "Book a room in an hotel.", description: "Book a room in an hotel.")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/hotelBooking/hotelBooking.js')
		jsApp.input = new Input(type: "service.hotelBooking.Booking", adapter: scriptAdapter)
		jsApp.output = new Output(type: "service.hotelBooking.BookingReceipt", adapter: scriptAdapter)
		return jsApp
	}
	
	@Bean
	EventHandler bookingRespons(){
		def eventHandler = new EventHandler(name: "bookingRespons")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'bookingReceipt.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.hotelBooking.BookingReceipt", adapter: fileAdapter)
		return eventHandler
	}

}

