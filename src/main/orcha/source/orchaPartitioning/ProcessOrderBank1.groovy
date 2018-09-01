package source.orchaPartitioning
title "process order bank1"

receive event from bankingOrder
compute processOrderBank1 with event.value
when "processOrderBank1 terminates"
send processOrderBank1.result to bankingTransaction
