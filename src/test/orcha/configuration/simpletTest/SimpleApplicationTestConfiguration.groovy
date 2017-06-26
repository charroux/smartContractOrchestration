package configuration.simpletTest

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
class SimpleApplicationTestConfiguration {
	
	@Bean
	EventHandler simpleApplicationTestInput(){
		def eventHandler = new EventHandler(name: "simpleApplicationTestInput")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "simpleApplicationTestInput.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "boolean", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	EventHandler simpleApplicationTestOutput(){
		def eventHandler = new EventHandler(name: "simpleApplicationTestOutput")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output', createDirectory: true, filename:'simpleApplicationTestOutput.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "boolean", adapter: fileAdapter)
		return eventHandler
	}
	
}
