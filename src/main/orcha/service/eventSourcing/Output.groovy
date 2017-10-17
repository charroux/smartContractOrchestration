package service.eventSourcing

import groovy.transform.ToString

@ToString
class Output {

	String s
	String output = "output"
	public Output(String s) {
		super();
		this.s = s;
	}
	
}
