title "Orcha service"

receive event from orchaProgramSource	 
compute orchaService with event.value
when "orchaService terminates"
send orchaService.result to orchaProgramDestination
