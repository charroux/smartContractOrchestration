package service.restWebService

import groovy.transform.ToString;
import groovy.transform.EqualsAndHashCode

@ToString
@EqualsAndHashCode
class Order implements Serializable{
	
	int number
	Product product

}

@ToString
@EqualsAndHashCode
class Product  implements Serializable{
	String specification
}
