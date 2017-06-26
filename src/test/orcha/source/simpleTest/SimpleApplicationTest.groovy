package source.simpleTest

title 'simple test'

receive variable from simpleApplicationTestInput
compute simpleServiceToTest with variable.value
when "simpleServiceToTest terminates with !=true"
send simpleServiceToTest.result to simpleApplicationTestOutput