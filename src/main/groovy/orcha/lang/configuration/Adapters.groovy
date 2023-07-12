package orcha.lang.configuration

import org.springframework.beans.factory.annotation.Autowired

import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;

enum UnidirectionalAdapter{
	Logging,
	File,
	FTP,
	SFTP,
	Feed,
	Stream,
	SQL,
	NoSQL,
	Mail,
	UDP,
	TCP,
	AMQP,
	MQTT,
	WebSocket,
	ComposeEvent
}

enum BidirectionalAdapter{
	HTTP,
	AMQP,
	WebService,
	JavaApplication,
	Blockchain,
	ScriptingApplication,
	MessagingMiddleware,
	OrchaApplication
}

public class ConfigurableProperties{
	def properties = []
}

/**
 * Allow to receive an event from local files.
 * @author Ben C.
 *
 */
@ToString
@EqualsAndHashCode
class InputFileAdapter extends ConfigurableProperties{
	
	def adapter = UnidirectionalAdapter.File
	/**
	 * The directory from wich files are read, Example: "C:/Users/BenC/data", Use / on any system.
	 */
	String directory
	/*
	 * the F							
	 */
	String filenamePattern
}

@ToString
@EqualsAndHashCode
class OutputFileAdapter extends ConfigurableProperties{
	
	public enum WritingMode{
		REPLACE,
		APPEND
	}
	
	def adapter = UnidirectionalAdapter.File
	String directory
	boolean createDirectory
	def filename
	boolean appendNewLine
	WritingMode writingMode = WritingMode.APPEND
	
	OutputFileAdapter(){
		super.properties.add("directory")
		super.properties.add("filename")
	}
}

@ToString
@EqualsAndHashCode
class MailSenderAdapter extends ConfigurableProperties{
	def adapter = UnidirectionalAdapter.Mail
	String to
	String subject = "Orcha"
	boolean sendAsAttachmentFile
	String attachmentFilename
	String host = "smtp.gmail.com"
	int port = 465
	String username			// orchalang@gmail.com
	String password
}


@ToString
@EqualsAndHashCode
class MailReceiverAdapter extends ConfigurableProperties{
	
	enum Protocol { IMAP, POP }
	
	def adapter = UnidirectionalAdapter.Mail
	Protocol protocol = Protocol.IMAP
	String host = "imap.gmail.com"
	int port = 993
	String inboxName = "inbox" 		// imaps://orchalang@gmail.com:password@imap.gmail.com:993/inbox
	String username					// orchalang@gmail.com
	String password
	//String subjectForFilteringEMails = "Orcha" 
	String subjectForFilteringEMails = "subject matches '(?i).*Orcha.*'"
	String directoryToCopyAttachmentFiles
	
}

@ToString
@EqualsAndHashCode
class WebServiceAdapter extends ConfigurableProperties{
	def adapter = BidirectionalAdapter.WebService
	def wsdl
}

@ToString
@EqualsAndHashCode
class HttpAdapter extends ConfigurableProperties{
	
	enum Method { GET, PUT, POST }
	
	def adapter = BidirectionalAdapter.HTTP
	def url
	def method
}

@ToString
@EqualsAndHashCode
class JavaServiceAdapter extends ConfigurableProperties{
	def adapter = BidirectionalAdapter.JavaApplication
	def javaClass 
	def method
}

@ToString
@EqualsAndHashCode
class BlockchainAdapter extends ConfigurableProperties{
	def adapter = BidirectionalAdapter.Blockchain
	def javaClass
	def method
}

@ToString
@EqualsAndHashCode
class ScriptServiceAdapter extends ConfigurableProperties{
	def adapter = BidirectionalAdapter.ScriptingApplication
	def file
}

/**
 * Allow to receive or sent an event to a message broker.
 * @author Ben C.
 *
 */
@ToString
@EqualsAndHashCode
class MessagingMiddlewareAdapter extends ConfigurableProperties{
	
	def adapter = BidirectionalAdapter.MessagingMiddleware
	boolean partitioned = false
	int instanceCount = 1		// number of instances of a the application to scale up
	int partitionNumber			// the number that identifies the partition
	String groupName			// generated automatically if not provided (required for partitioning)

}

@ToString
@EqualsAndHashCode
class OrchaServiceAdapter extends ConfigurableProperties{
	def adapter = BidirectionalAdapter.OrchaApplication
	EventHandler inputEventHandler	// event for service response					// a registry should be used to discover the handler dynamically
	EventHandler outputEventHandler	// event for service calling					// a registry should be used to discover the handler dynamically
}

@EqualsAndHashCode
class DataSource{
	def driver
	def url
	def username
	def password
}

@ToString
@EqualsAndHashCode
class DatabaseAdapter extends ConfigurableProperties{	
	def adapter = UnidirectionalAdapter.SQL
	DataSource dataSource
	def request
}

@ToString
@EqualsAndHashCode
class AMQP_Adapter extends ConfigurableProperties{
	def adapter = BidirectionalAdapter.AMQP
	def queueName
	def host
}

@ToString
@EqualsAndHashCode
class LoggingAdapter extends ConfigurableProperties{
	
	enum Level { INFO }
	
	def adapter = UnidirectionalAdapter.Logging
	def level = Level.INFO
}

@ToString
@EqualsAndHashCode
class ComposeEventAdapter extends ConfigurableProperties{
	
	def adapter = UnidirectionalAdapter.ComposeEvent
	
}
