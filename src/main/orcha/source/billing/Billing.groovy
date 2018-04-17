package source.billing

domain productSales
description "Billing of an order."
title "billing order"
author "Ben C."
version "1.0"

receive order from preparedOrder condition "delay < 5"
compute billing with order.value

when "billing terminates"		
send billing.result to accounting
