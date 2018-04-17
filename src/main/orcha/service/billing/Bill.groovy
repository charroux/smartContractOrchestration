package service.billing

import service.prepareOrder.PreparedOrder

class Bill {
	
	PreparedOrder preparedOrder 
	Date date
	float price
	
	public Bill(PreparedOrder preparedOrder, float price) {
		super();
		this.preparedOrder = preparedOrder
		this.price = price
		this.date = Calendar.getInstance().getTime()
	}

}
