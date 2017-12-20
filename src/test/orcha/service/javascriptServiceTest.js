
function javascriptService(person) {
	var Employee = Java.type("service.javascript.Employee");	// tested with the java's standard javascript engine embedded into java 1.8.
	return new Employee(person, 1000);
}

javascriptService(payload);