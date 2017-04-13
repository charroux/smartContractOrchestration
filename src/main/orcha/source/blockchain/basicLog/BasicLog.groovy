package source.blockchain.basicLog

receive event from basicLogInputFile
compute service4 with event.value

when "service4 terminates"
send service4.result to basicLogOutputFile