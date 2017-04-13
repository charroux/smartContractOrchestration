#!/usr/bin/env node

var amqp = require('amqplib/callback_api');

amqp.connect('amqp://localhost', function(err, conn) {
  conn.createChannel(function(err, ch) {
    var q = 'rpc_queue';

    ch.assertQueue(q, {durable: false});
    ch.prefetch(1);
    console.log('Awaiting RPC requests');
    ch.consume(q, function reply(msg) {
    	
    	var res = JSON.parse(msg.content.toString());
    	
    	console.log("JSon received: ")
    	console.log(res);
    	
    	res.price = 200;
    	
    	console.log("JSon changed: ");
    	console.log(res);

      ch.sendToQueue(msg.properties.replyTo,
    		  new Buffer(JSON.stringify(res)),
        {correlationId: msg.properties.correlationId},
        {contentType: 'application/json'});

      ch.ack(msg);
    });
  });
});

function fibonacci(n) {
  if (n == 0 || n == 1)
    return n;
  else
    return fibonacci(n - 1) + fibonacci(n - 2);
}
