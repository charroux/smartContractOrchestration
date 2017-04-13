/**
 * 
 */

function createPersistentMessage(composeEvent) {
	
	var currentValue = composeEvent.getValue();
	persistentMessage.setMessage(currentValue);
	
	persistentMessage.setTimestamp(composeEvent.getTimestamp())
	
	return persistentMessage;
	
}

createPersistentMessage(payload);