package service.javascript

import groovy.transform.ToString

@ToString
class Employee {
	
	Person person
	int salary
	public Employee(Person person, int salary) {
		super();
		this.person = person;
		this.salary = salary;
	}
	
	

}
