package source.prepareOrder

title 'test for PrepareOrder'

receive order from customer condition "order.product.specification == 'TV'"
compute prepareOrder with order.value
when "prepareOrder terminates condition delay>10"
send prepareOrder.result to delivery