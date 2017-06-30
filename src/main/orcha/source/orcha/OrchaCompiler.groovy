package source.orcha
				
domain orcha
description "orcha compiler"
title "orcha compiler"
author "Ben C."
version "1.0"

receive orchaProgram from orchaFile
compute parseOrcha with orchaProgram.value

when "parseOrcha terminates"
compute generateServiceOfferSelection with parseOrcha.result

when "parseOrcha terminates"		
compute generateMockOfServices with parseOrcha.result

/*when "generateMockOfServices terminates condition ==false"
compute compileForLaunching with parseOrcha.result

when "compileForLaunching terminates"
compute generateConfigurationProperties with parseOrcha.result

when "generateConfigurationProperties terminates"
compute searchForOrchaTestingFile with orchaProgram.value

when "searchForOrchaTestingFile terminates"
compute compileForTesting with searchForOrchaTestingFile.result
*/

