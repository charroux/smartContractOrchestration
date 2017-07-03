package source.simpleTest

title 'simple application to test'

receive variable from simpleApplicationInput
compute simpleApplicationService with variable.value
when "simpleApplicationService terminates with !=true"
send simpleApplicationService.result to simpleApplicationOutput