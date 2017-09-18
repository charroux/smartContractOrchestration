title "benchmarking services"

receive event from benchmarkingInputFile	 
compute codeToBenchmark1 with event.value
receive event from benchmarkingInputFile
compute codeToBenchmark2 with event.value
when "codeToBenchmark1 terminates and codeToBenchmark2 terminates"
send codeToBenchmark1.result to benchmarkingOutputFile
