package service

import groovy.transform.ToString;

@ToString
class CopyOfProduct implements Serializable {
	
	String name
	
	public CopyOfProduct(String name) {
		super();
		this.name = name;
	}

	public CopyOfProduct() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
