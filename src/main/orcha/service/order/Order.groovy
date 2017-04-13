package service.order

import groovy.transform.ToString;

/**
 * This class is equivalent to a JSon data structure.
 * 
 * For example:
 * {
 *	"number":1,
 *	"product":{
 *		"specification":"TV"
 *	}
 * }
 * 
 * @author Ben C.
 *
 */
@ToString
class Order {
	
	int number
	Product product

}

@ToString
class Product{
	String specification
}
