package source.callingServiceByEMail

receive event from input1
compute service2 with event.value
when "service2 terminates"
send service2.result to output1