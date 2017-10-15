title "computes in series"
description "Read the content of a text file. Pass its content to a service. Launch another service in a serie. Then write the result of the lastest service to a file."

receive event from computesInSeriesInputFile			
compute firstProgram with event.value
when "firstProgram terminates"
compute secondProgram with firstProgram.result
when "secondProgram terminates"
send secondProgram.result to computesInSeriesOutputFile
