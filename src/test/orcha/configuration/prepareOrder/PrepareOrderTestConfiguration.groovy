package configuration.prepareOrder

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import groovy.util.logging.Slf4j
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@Configuration
@Slf4j
class PrepareOrderTestConfiguration {
	
	@Bean
	EventHandler aCustomerTest(){
		def eventHandler = new EventHandler(name: "aCustomerTest")
		def fileAdapter = new InputFileAdapter(directory: 'data/test/input', filenamePattern: "orderToPrepareTest.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.prepareOrder.Order", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler prepareOrderTestErrorReport(){
		def eventHandler = new EventHandler(name: "prepareOrderTestErrorReport")
		def fileAdapter = new OutputFileAdapter(directory: 'data/test/output', createDirectory: true, filename:'prepareOrderTestErrorReport.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.prepareOrder.PreparedOrder", adapter: fileAdapter)
		return eventHandler
	}

}
