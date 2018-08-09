package source.orchaService
title "Orcha service"

receive event from orchaProgramSource condition "specification == 'TV'"
compute orchaService with event.value
when "orchaService terminates"
send orchaService.result to orchaProgramDestination
