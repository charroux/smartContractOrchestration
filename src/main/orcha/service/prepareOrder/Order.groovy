package service.prepareOrder

import groovy.transform.ToString;
import groovy.transform.EqualsAndHashCode

@ToString
@EqualsAndHashCode
class Order {
	
	int number
	Product product

}

@ToString
@EqualsAndHashCode
class Product{
	String specification
}
