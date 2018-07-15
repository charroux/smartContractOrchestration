title "Orcha service"

receive event from orchaProgram	 
compute orchaService with event.value
when "orchaService terminates"
send orchaService.result to orchaServiceOutputFile
