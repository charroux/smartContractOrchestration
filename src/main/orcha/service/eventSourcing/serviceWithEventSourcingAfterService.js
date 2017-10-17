
function serviceWithEventSourcingAfterService(input) {
	print('serviceWithEventSourcingAfterService receives:' + input.s);
	var Intermediate = Java.type("service.eventSourcing.Intermediate");
	return new Intermediate(input.s + " from serviceWithEventSourcingAfterService");
}

serviceWithEventSourcingAfterService(payload);