package service.order

import groovy.transform.ToString;
import groovy.transform.EqualsAndHashCode

/**
 * This class is equivalent to a JSon data structure.
 * 
 * For example:
 * {
 *	"number":2,
 *	"product":{
 *		"specification":"TV"
 *	}
 * }
 * 
 * @author Ben C.
 *
 */
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

