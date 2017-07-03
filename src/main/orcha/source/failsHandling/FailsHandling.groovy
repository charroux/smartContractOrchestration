package source.failsHandling

title "fails handling"

receive event from anInputFile
compute aService with event.value

receive event from anInputFile
compute anotherService with event.value

when "aService terminates and anotherService terminates"
compute finalService with aService.result, anotherService.result 

when "aService fails"
compute alternativeService with aService.error.originalMessage

when "anotherService fails"
send anotherService.error to anErrorFile

when "finalService terminates"
send finalService.result to anOutputFile

when "alternativeService terminates"
send alternativeService.result to anOutputFile