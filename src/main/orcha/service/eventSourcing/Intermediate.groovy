package service.eventSourcing

import groovy.transform.ToString

@ToString
class Intermediate {

	String s
	String intermediate = "intermediate"
	public Intermediate(String s) {
		super();
		this.s = s;
	}
	
}
