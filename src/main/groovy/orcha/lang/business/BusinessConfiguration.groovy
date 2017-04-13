package orcha.lang.business

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

import groovy.transform.ToString

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Component
@ToString
class BusinessConfiguration {
	
	//List<String> prepareCommand = new ArrayList<String>()
	
	def instruction = [:]

}
