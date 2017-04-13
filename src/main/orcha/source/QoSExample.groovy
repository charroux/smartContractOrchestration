				
receive composeEvent from composeEvents whose "composeEvent.state == INCOMING_MESSAGE"
compute createPersistentMessage with composeEvent.value, persistentMessage
when "createPersistentMessage terminates"
send createPersistentMessage.result to messagePersistenceDataBase
