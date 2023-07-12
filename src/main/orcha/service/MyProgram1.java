package service;

public class MyProgram1 {
	
	static int numberOfFailures=0;
			
	public int myMethod(int i){
		System.out.println("MyProgram receives " + i);
		if(numberOfFailures<8){
			numberOfFailures++;
			throw new RuntimeException("numberOfFailures: " + numberOfFailures);
		}
		System.out.println("MyProgram returns " + i);
		return i;
	}

}
