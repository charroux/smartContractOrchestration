package source.javascript

title "javascript service"

receive event from javascriptServiceInputFile	 
compute javascriptService with event.value
when "javascriptService terminates"
send javascriptService.result to javascriptServiceOutputFile
