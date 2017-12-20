
function serviceWithEventSourcingBeforeService(intermediate) {
	print('serviceWithEventSourcingBeforeService receives:' + intermediate.s);
	var Output = Java.type("service.eventSourcing.Output");
	return new Output(intermediate.s + " from serviceWithEventSourcingBeforeService");
}

serviceWithEventSourcingBeforeService(payload);