title "benchmarking services"
description "Read a tesxt file, dispatch its content to two services, then launch the two services and wait until the two services complete. Write the result to a service into a file."

receive event from benchmarkingInputFile condition "!=0"	 
compute codeToBenchmark1 with event.value
receive event from benchmarkingInputFile condition "!=0"
compute codeToBenchmark2 with event.value
when "(codeToBenchmark2 terminates condition == 1) and (codeToBenchmark1 terminates condition == -1)"
send codeToBenchmark1.result to benchmarkingOutputFile
