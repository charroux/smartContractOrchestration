package configuration.simpleTest

import groovy.util.logging.Slf4j
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Slf4j
class SimpleApplicationConfiguration {
	
	@Bean
	EventHandler simpleApplicationInput(){
		def eventHandler = new EventHandler(name: "simpleApplicationInput")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "simpleApplicationInput.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "boolean", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application simpleApplicationService(){
		def jsApp = new Application(name: "simpleApplicationService", language: "js")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/main/orcha/service/simpleTest/simpleApplicationService.js')
		jsApp.input = new Input(type: "boolean", adapter: scriptAdapter)
		jsApp.output = new Output(type: "boolean", adapter: scriptAdapter)
		return jsApp
	}
	
	@Bean
	EventHandler simpleApplicationOutput(){
		def eventHandler = new EventHandler(name: "simpleApplicationOutput")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'simpleApplicationOutput.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "boolean", adapter: fileAdapter)
		return eventHandler
	}
	
}