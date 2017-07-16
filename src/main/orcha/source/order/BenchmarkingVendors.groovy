package source.order
				
/**
 * This file is written in Orcha language.
 * It should be written by a business analyst.
 * 
 * The configuration of this program is in the folder configuration/order.
 * It should be done by an operational.
 * 
 * The related programs are in the folder service/order.
 * They should be written by a developper. 
 */

// condition acts as a filter: only product TV will be processed
// any other product will be ignored.
// The when instruction waits for program orderConverter to be terminated.
// So the comp
domain productSales
description "Choose among TV vendors the cheapest one."
title "select best TV vendors"
author "Ben C."
version "1.0"

receive order from customer condition "order.product.specification == 'TV'"
compute orderConverter with order.value

when "orderConverter terminates"		
compute vendor1 with orderConverter.result

// The following instruction can be repeated any number of times (3 times in this program).
// So the same event order will be dispatched to any instructions following the receive instruction.
receive order from customer condition "order.product.specification == 'TV'"
compute vendor2 with order.value

receive order from customer condition "order.product.specification == 'TV'"
compute vendor3 with order.value

when "(vendor1 terminates) and (vendor2 terminates) and (vendor3 terminates)"
compute selectBestVendor with vendor1.result, vendor2.result, vendor3.result

when  "selectBestVendor terminates condition price>1000"
send selectBestVendor.result to outputFile1

when  "selectBestVendor terminates condition price<=1000"
send selectBestVendor.result to outputFile2
