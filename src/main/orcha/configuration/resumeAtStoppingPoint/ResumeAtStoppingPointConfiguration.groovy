package configuration.resumeAtStoppingPoint

import orcha.lang.configuration.Application;
import orcha.lang.configuration.EventHandler;
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.EventSourcing.MessageStore

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import service.MyProgram;
import service.resumeAtStoppingPoint.Class1
import service.resumeAtStoppingPoint.Class2

@EventSourcing(messageStore=MessageStore.mongoDB, eventName="", resumeAtStoppingPoint=true)
class PersistentApplication1 extends Application{
}

@EventSourcing(messageStore=MessageStore.mongoDB, eventName="", resumeAtStoppingPoint=true)
class PersistentApplication2 extends Application{
}

@Configuration
class ResumeAtStoppingPointConfiguration {
	
	@Bean
	EventHandler in1(){
		def eventHandler = new EventHandler(name: "in1")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/in1', filenamePattern: "data.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}
	
	@Bean
	Application s1(){
		def program = new PersistentApplication1(name: "s1", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.resumeAtStoppingPoint.Class1', method:'method1')
		program.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program
	}
	
	@Bean
	Class1 class1(){
		return new Class1()
	}
	
	@Bean
	Application s2(){
		def program = new PersistentApplication2(name: "s2", language: "Groovy")
		def javaAdapter = new JavaServiceAdapter(javaClass: 'service.resumeAtStoppingPoint.Class2', method:'method2')
		program.input = new Input(type: "java.lang.Integer", adapter: javaAdapter)
		program.output = new Output(type: "java.lang.Integer", adapter: javaAdapter)
		return program
	}
	
	@Bean
	Class2 class2(){
		return new Class2()
	}
	
	@Bean
	EventHandler out1(){
		def eventHandler = new EventHandler(name: "out1")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/out1', createDirectory: true, filename:'out1.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "java.lang.Integer", adapter: fileAdapter)
		return eventHandler
	}

}
