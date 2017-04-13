/**
 * 
 */

function vendor2(order) {
	var billJSon = '{ "date":"", "number":"", "description":"", "price":1100, "vendor":"vendor2" }';
	var bill = JSON.parse(billJSon);
	bill.date = new Date().getTime();
	bill.number = order.number;
	bill.description = order.product.specification;
	// A Java object must be returned in order to persist the message into a message store
	var Bill = Java.type("service.order.Bill")
	return new Bill(bill.date, bill.number, bill.vendor, bill.description, bill.price);
}

vendor2(payload);