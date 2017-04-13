package source.callingServiceByEMail

receive event from input1
compute service1 with event.value
when "service1 terminates"
send service1.result to output1