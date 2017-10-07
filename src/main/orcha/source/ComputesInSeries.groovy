title "computes in series"

receive event from computesInSeriesInputFile			
compute firstProgram with event.value
when "firstProgram terminates"
compute secondProgram with firstProgram.result
when "secondProgram terminates"
send secondProgram.result to computesInSeriesOutputFile
