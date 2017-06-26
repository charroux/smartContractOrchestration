package source.simpleTest

title 'simple application to test'

receive variable from simpleApplicatonToTestInput
compute simpleServiceToTest with variable.value
when "simpleServiceToTest terminates with result!=true"
send simpleServiceToTest.result to simpleApplicationToTestOutput