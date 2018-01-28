package source.prepareOrderTest

domain productSales
description "Expected behavior for prepare an order."
title "prepare order behavior"
author "Ben C."
version "1.0"

receive product from oneTVFile
compute prepareAnOrder with product.value

when "prepareAnOrder terminates"		
send prepareAnOrder.result to prepareOrderTestReport
