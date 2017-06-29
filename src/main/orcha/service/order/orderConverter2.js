
function orderConverter2(order) {
	return new SpecificOrder(order.number, order.product.specification);
}

orderConverter2(payload);

