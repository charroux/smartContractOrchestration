package source.javascript

title "javascript service"
description "Read a Json file. Pass its content to a Javascript service. Launch the service, then write the result of the service to a Json file."

receive event from javascriptServiceInputFile	 
compute javascriptService with event.value
when "javascriptService terminates"
send javascriptService.result to javascriptServiceOutputFile
