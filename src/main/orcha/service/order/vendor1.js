
function vendor1(specificOrder) {
	var billJSon = '{ "date":"", "number":0, "description":"", "price":1200, "vendor":"vendor1" }';
	//throw 5
	var bill = JSON.parse(billJSon);
	bill.date = new Date().getTime();
	bill.number = specificOrder.number;
	bill.description = specificOrder.product;
	// A Java object must be returned in order to persist the message into a message store
	var Bill = Java.type("service.order.Bill");
	return new Bill(bill.date, bill.number, bill.vendor, bill.description, bill.price);
}

vendor1(payload);