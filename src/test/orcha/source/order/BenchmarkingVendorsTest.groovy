package source.order
				
title 'test data for BenchmarkingVendors'

receive order from orderConverterInputTest
compute orderConverter with order.value
when "orderConverter terminates condition product=='TV'"
send orderConverter.result to testErrorReport

