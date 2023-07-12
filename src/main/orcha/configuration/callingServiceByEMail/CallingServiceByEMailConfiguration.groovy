package configuration.callingServiceByEMail

import orcha.lang.configuration.Application;
import orcha.lang.configuration.EventHandler;
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.MailReceiverAdapter
import orcha.lang.configuration.MailSenderAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode
import orcha.lang.configuration.EventSourcing
import orcha.lang.configuration.EventSourcing.MessageStore
import orcha.lang.configuration.EventSourcing.JoinPoint

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import service.failsHandling.ServiceComparison;

//@EventSourcing(messageStore=MessageStore.mongoDB, joinPoint=JoinPoint.before, eventName="")
class PersistentApplication extends Application{
}

@Configuration
class CallingServiceByEMailConfiguration {

	@Bean
	EventHandler input1(){
		def eventHandler = new EventHandler(name: "input1")
		def fileAdapter = new InputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/input1', filenamePattern: "data.json")
		eventHandler.input = new Input(mimeType: "application/json", type: "service.callingServiceByEMail.Person", adapter: fileAdapter)
		return eventHandler
	}	
	
	@Bean
	Application service2(){
		def program = new PersistentApplication(name: "service1", language: "Orcha")
		def mailSenderAdapter = new MailSenderAdapter(to: "orchalang2@gmail.com", sendAsAttachmentFile: true, attachmentFilename: "data.json", username: "orchalang1@gmail.com", password: "Wasters1234")
		program.input = new Input(type: "service.callingServiceByEMail.Person", adapter: mailSenderAdapter)
		//def mailReceiverAdapter = new MailReceiverAdapter(username: "orchalang1@gmail.com", password: "Wasters1234", subjectForFilteringEMails: "subject matches 'Orcha'", directoryToCopyAttachmentFiles: "C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/mailAttachement")
		def mailReceiverAdapter = new MailReceiverAdapter(username: "orchalang1@gmail.com", password: "Wasters1234", directoryToCopyAttachmentFiles: "C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/mailAttachement")
		program.output = new Output(type: "service.callingServiceByEMail.Customer", adapter: mailReceiverAdapter)
		return program
	}
	
	@Bean
	EventHandler output1(){
		def eventHandler = new EventHandler(name: "output1")
		def fileAdapter = new OutputFileAdapter(directory: 'C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/output1', createDirectory: true, filename:'output1.json', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "application/json", type: "service.callingServiceByEMail.Customer", adapter: fileAdapter)
		return eventHandler
	}
	
}
