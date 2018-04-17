/**
 * 
 */

function billing(preparedOrder) {
	var Bill = Java.type("service.billing.Bill");	// tested with the java's standard javascript engine embedded into java 1.8.
	var bill = new Bill(preparedOrder, 232.5);
	return bill;
}

billing(payload);