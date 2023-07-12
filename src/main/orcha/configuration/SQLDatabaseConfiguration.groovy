package configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.DataSource
import orcha.lang.configuration.DatabaseAdapter
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import service.MyProgram1

@Configuration
class SQLDatabaseConfiguration {
	
	@Bean
	EventHandler file(){
		def eventHandler = new EventHandler(name: "file")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input', filenamePattern: "*.txt")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	MyProgram1 myProgram(){
		return new MyProgram1()
	}
	
	@Bean
	Application code(){
		def codeApplication = new Application(name: "code", language: "Java")
		def javaCodeAdapter = new JavaServiceAdapter(javaClass: 'service.MyProgram1', method:'myMethod')
		def codeInput = new Input(type: "java.lang.Integer", adapter: javaCodeAdapter)
		codeApplication.input = codeInput
		def codeOutput = new Output(type: "java.lang.Integer", adapter: javaCodeAdapter)
		codeApplication.output = codeOutput
		return codeApplication
	}
	
	@Bean
	EventHandler database(){
		def eventHandler = new EventHandler(name: "database")
		def request = "insert into RESULT_TABLE (payload) values (:payload)"
		def dataSource = new DataSource(driver: "org.h2.Driver", url: "jdbc:h2:tcp://localhost/~/test", username: "sa", password: "")
		def databaseAdapter = new DatabaseAdapter(dataSource: dataSource, request: request)
		eventHandler.output = new Output(type: "TABLE RESULT_TABLE (payload INTEGER)", adapter: databaseAdapter)
		return eventHandler
	}

}
