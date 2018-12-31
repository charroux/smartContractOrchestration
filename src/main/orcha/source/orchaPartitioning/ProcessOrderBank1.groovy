package source.orchaPartitioning
title "process order bank1"

receive event from bankingOrder
compute processOrderBank with event.value
when "processOrderBank terminates"
send processOrderBank.result to bankingTransaction
