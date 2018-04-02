package service.restWebService

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
public class PreparedOrder implements Serializable{
	
	String address	// address for delivery	Order order
	int delay
}
