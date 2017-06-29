package configuration.order

import groovy.util.logging.Slf4j
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Slf4j
class BenchmarkingVendorsTestConfiguration {
	
	@Bean
	EventHandler orderConverterInputTest(){
		def eventHandler = new EventHandler(name: "orderConverterInputTest")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "orderConverterInputTest.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.order.Order", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler testErrorReport(){
		def eventHandler = new EventHandler(name: "testErrorReport")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'testErrorReport.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.order.Bill", adapter: fileAdapter)
		return eventHandler
	}
	
}
