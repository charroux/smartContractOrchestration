package source.orchaPartitioning

title "bank service"

receive order from bankCustomer condition "bank == 'BANK1'"
compute processOrderBank1 with order.value

when "processOrderBank1 terminates"		
send processOrderBank1.result to bankCustomer1

receive order from bankCustomer condition "bank == 'BANK2'"
compute processOrderBank2 with order.value

when "processOrderBank2 terminates"		
send processOrderBank2.result to bankCustomer1

receive order from bankCustomer condition "bank == 'BANK3'"
compute processOrderBank3 with order.value

when "processOrderBank3 terminates"
send processOrderBank3.result to bankCustomer1

