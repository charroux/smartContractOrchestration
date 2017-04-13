package service.order

import groovy.transform.ToString;

@ToString
class Bill {
	
	long date
	int number
	String vendor
	String description
	float price
	/**
	 * Useful since Javascript codes should return an Bill object in order to persist the message into a message store (see Javascript codes). 
	 * @param date
	 * @param number
	 * @param vendor
	 * @param description
	 * @param price
	 */
	public Bill(long date, int number, String vendor, String description, float price) {
		super();
		this.date = date;
		this.number = number;
		this.vendor = vendor;
		this.description = description;
		this.price = price;
	}
	
	

}
