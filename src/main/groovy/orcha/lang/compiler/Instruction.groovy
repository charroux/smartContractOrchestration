package orcha.lang.compiler

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode
@ToString
class Instruction {
	
	int id
	String instruction			// receive						compute				send
	String variable				// event of receive									code1 of send
	String variableProperty		//													result of code1
	def springBean				// EventHandler of receive		code1 of compute	EventHandler of receive
	def withs = []
	String condition
	
	@ToString
	class With{
		def with
		def withProperty
	}
	
	boolean containsWith(def with){
		int i=0
		while(i<withs.size() && withs.get(i).with!=with){
			i++
		}
		if(i <  withs.size()){
			return true
		} else {
			return false
		}
	}


}
