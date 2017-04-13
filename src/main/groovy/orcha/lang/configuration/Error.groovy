package orcha.lang.configuration

import groovy.transform.ToString;

@ToString
class Error {
	
	def originalMessage
	String message
	def exception

}
