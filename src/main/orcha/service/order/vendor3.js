/**
 * 
 */

function vendor3(order) {
	var billJSon = '{ "date":"", "number":"", "description":"", "price":1005, "vendor":"vendor3" }';
	var bill = JSON.parse(billJSon);
	bill.date = new Date().getTime();
	bill.number = order.number;
	bill.description = order.product.specification;
	// A Java object must be returned in order to persist the message into a message store
	var Bill = Java.type("service.order.Bill")
	return new Bill(bill.date, bill.number, bill.vendor, bill.description, bill.price);
}

vendor3(payload);