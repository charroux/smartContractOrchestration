package source.prepareOrder

domain productSales
description "Prepare an order."
title "prepare order"
author "Ben C."
version "1.0"

receive order from customer
compute prepareOrder with order.value

when "prepareOrder terminates"		
send prepareOrder.result to delivery