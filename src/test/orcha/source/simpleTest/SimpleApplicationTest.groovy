package source.simpleTest

title 'simple test'

receive variable from simpleApplicationTestInput
compute simpleApplicationService with variable.value
when "simpleApplicationService terminates condition ==true"
send simpleApplicationService.result to simpleApplicationTestOutput