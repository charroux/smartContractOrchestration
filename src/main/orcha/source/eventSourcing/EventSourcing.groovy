package source.eventSourcing

title "event sourcing"
description "Store into a NoSQL database (MongoDB): the input event (coming from a Json file), the output of a first service call, the input of a second service call and the output event."

receive event from eventSourcingInputFile	
 
compute serviceWithEventSourcingAfterService with event.value

when "serviceWithEventSourcingAfterService terminates"
compute serviceWithEventSourcingBeforeService with serviceWithEventSourcingAfterService.result

when "serviceWithEventSourcingBeforeService terminates"
send serviceWithEventSourcingBeforeService.result to eventSourcingOutputFile
