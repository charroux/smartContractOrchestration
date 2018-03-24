package source.restWebService

title "order over http"

receive event from remoteCustomerOverHttp
compute preparingOrder with event.value
when "preparingOrder terminates"
send preparingOrder.result to httpResponse
