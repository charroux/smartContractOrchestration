package source.orcha
				
domain orcha
description "orcha compiler"
title "orcha compiler"
author "Ben C."
version "1.0"

receive orchaFile from orchaFolder
compute parseOrchaFile with orchaFile.value

when "orchaFile terminates"		
compute generateMockOfServices with parseOrchaFile.result

when "generateMockOfServices terminates condition ==true"
compute compileOrcha with parseOrchaFile.result

when "compileOrcha terminates"
compute generateConfigurationProperties with parseOrchaFile.result

when "generateConfigurationProperties terminates"
compute searchForOrchaTestingFile with orchaFile.value

when "searchForOrchaTestingFile terminates"
compute compileOrcha with searchForOrchaTestingFile.result


