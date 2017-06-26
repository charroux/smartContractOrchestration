package source.simpleTest

title 'simple test'

receive variable from simpleApplicationTestInput
compute simpleServiceToTest with variable.value
when "simpleServiceToTest terminates condition !=true"
send simpleServiceToTest.result to simpleApplicationTestOutput