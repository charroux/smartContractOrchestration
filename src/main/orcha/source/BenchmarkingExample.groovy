title "benchmarking services"
description "Read a tesxt file, dispatch its content to two services, then launch the two services and wait until the two services complete. Write the result to a service into a file."

receive event from benchmarkingInputFile	 
compute codeToBenchmark1 with event.value
receive event from benchmarkingInputFile
compute codeToBenchmark2 with event.value
when "(codeToBenchmark1 terminates condition == -1) and (codeToBenchmark2 terminates condition == 1)"
send codeToBenchmark1.result to benchmarkingOutputFile
