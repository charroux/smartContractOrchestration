package service.restWebService

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
public class PreparedOrder {
	
	String address	// address for delivery
	int delay
}