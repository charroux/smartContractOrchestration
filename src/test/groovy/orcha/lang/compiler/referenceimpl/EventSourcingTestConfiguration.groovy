package orcha.lang.compiler.referenceimpl

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.EventSourcing.MessageStore
import orcha.lang.configuration.EventSourcing.JoinPoint
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.ScriptServiceAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

/**
 * MongoDB must be installed to use this example: https://docs.mongodb.com/getting-started/shell/
 * Start mongoDB:
 * from a command line window start MongoDB using (from the MongoDB’s bin directory): mongod --dbpath optionalPathToADataDirectory
 * Inspect the database:
 * from a command line window: 
 * 		mongo
 * 		show dbs
 * 		use OrchaEventSourcing
 * 		show collections
 * 		db.message.find()
 * 
 * 		db.message.drop() to drop the content
 * 
 * @author Ben C.
 *
 */

/**
 * The configuration to store the input event into MongoDB (note the joinPoint set to after)
 * @author Ben C.
 *
 */
@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.after, eventName="")
class EventSourcingInputEventHandler extends EventHandler{
}

/**
 * The configuration to store the output event of the first service into MongoDB (note the joinPoint set to after).
 * JoinPoint.beforeAndAfter can also be used.
 * @author Ben C.
 *
 */
@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.after, eventName="")
class ApplicationWithEventSourcingAfterService extends Application{
}

/**
 * The configuration to store the input event of the second service into MongoDB (note the joinPoint set to before)
 * @author Ben C.
 *
 */
@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.before, eventName="")
class ApplicationWithEventSourcingBeforeService extends Application{
}

/**
 * The configuration to store the output into MongoDB (note the joinPoint set to before)
 * @author Ben C.
 *
 */
@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.before, eventName="")
class EventSourcingOutputEventHandler extends EventHandler{
}

@Configuration
trait EventSourcingTestConfiguration {
	
	@Bean
	EventHandler eventSourcingInputFile(){
		def eventHandler = new EventSourcingInputEventHandler(name: "eventSourcingInputFile")
		def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "eventSourcingInputFile.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.eventSourcing.Input", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application serviceWithEventSourcingAfterService(){
		def jsApp = new ApplicationWithEventSourcingAfterService(name: "serviceWithEventSourcingAfterService", language: "js", specifications: "", description: "")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/test/orcha/service/serviceWithEventSourcingAfterServiceTest.js')
		jsApp.input = new Input(type: "service.eventSourcing.Input", adapter: scriptAdapter)
		jsApp.output = new Output(type: "service.eventSourcing.Intermediate", adapter: scriptAdapter)
		return jsApp
	}
	
	@Bean
	Application serviceWithEventSourcingBeforeService(){
		def jsApp = new ApplicationWithEventSourcingBeforeService(name: "serviceWithEventSourcingBeforeService", language: "js", specifications: "", description: "")
		def scriptAdapter = new ScriptServiceAdapter(file: 'file:src/test/orcha/service/serviceWithEventSourcingBeforeServiceTest.js')
		jsApp.input = new Input(type: "service.eventSourcing.Intermediate", adapter: scriptAdapter)
		jsApp.output = new Output(type: "service.eventSourcing.Output", adapter: scriptAdapter)
		return jsApp
	}
	
	@Bean
	EventHandler eventSourcingOutputFile(){
		def eventHandler = new EventSourcingOutputEventHandler(name: "eventSourcingOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'eventSourcingOutputFile.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.eventSourcing.Output", adapter: fileAdapter)
		return eventHandler
	}

}
