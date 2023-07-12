package configuration.qos

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import service.qos.Service
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import orcha.lang.configuration.OutputFileAdapter.WritingMode

@Configuration
class QoSConfiguration {
	
	/*@Bean
	Service service(){
		return new Service()
	}*/
	
	/*@Bean
	EventHandler qosOutputFile(){
		def eventHandler = new EventHandler(name: "qosOutputFile")
		def fileAdapter = new OutputFileAdapter(directory: 'data/output', createDirectory: true, filename:'qosOutputFile.txt', appendNewLine: true, writingMode: WritingMode.REPLACE)
		eventHandler.output = new Output(mimeType: "text/plain", type: "java.lang.String", adapter: fileAdapter)
		return eventHandler
	}*/

}
