package source.orderConverter
title "selectOrderConverter"
receive order from orderEventHandler
compute orderConverter1 with order.value
receive order from orderEventHandler
compute orderConverter2 with order.value
when "(orderConverter1 terminates) and (orderConverter2 terminates)"
compute selectOrderConverter with orderConverter1.result, orderConverter2.result
when "selectOrderConverter terminates"
send selectOrderConverter.result to orderConverterOutput
