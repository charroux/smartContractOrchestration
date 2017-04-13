package service

import groovy.transform.ToString;

@ToString
class Product{
	
	String name
	
}

@ToString
class Order{
	
	Product product
	def price
	
}
