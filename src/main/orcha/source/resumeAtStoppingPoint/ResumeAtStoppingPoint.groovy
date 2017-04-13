package source.resumeAtStoppingPoint

receive event from in1
compute s1 with event.value
when "s1 terminates"
compute s2 with s1.result
when "s2 terminates"
send s2.result to out1
