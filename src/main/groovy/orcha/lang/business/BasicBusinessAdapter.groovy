package orcha.lang.business

import org.springframework.beans.factory.annotation.Autowired;

import groovy.util.logging.Slf4j

@Slf4j
class BasicBusinessAdapter implements BusinessAdapter{
	
	@Autowired
	BusinessConfiguration businessConfiguration

	@Override
	public String adaptOrchaFileToBusiness(String orchaFile) {
		
		//String pathToCode = "." + File.separator + "orcha" + File.separator + "source" + File.separator + orchaFile
		String pathToCode = "." + File.separator + "src" + File.separator + "main" + File.separator + "orcha" + File.separator + "source" + File.separator + orchaFile
		
		log.info 'Conversion of the Orcha program ' + pathToCode + ' to a Groovy program'
		
		def wordsToReplace = [:]
		def lines = []
		
		new File(pathToCode).eachLine { line ->
			
			String instruction = line
			
			instruction = instruction.trim()
			
			if(instruction.startsWith("receive")==false && instruction.startsWith("compute")==false && instruction.startsWith("when")==false 
				&& instruction.startsWith("send")==false && instruction.startsWith("//")==false 
				&& instruction.startsWith("/*")==false && instruction.startsWith("*")==false && instruction.startsWith("*/")==false
				&& instruction.startsWith("package")==false && instruction.size()>0){
				
				int index = instruction.indexOf(" ")
				
				String wordToReplace = instruction.substring(0, index)
				
				String newInstruction =  this.instructionToReplace(wordToReplace)
				
				String s = newInstruction.substring(newInstruction.indexOf("compute ") + "compute ".length()).trim()
				
				wordsToReplace.put(wordToReplace, s)
				
				newInstruction = newInstruction + " " + instruction.substring(index + 1)
				
				wordsToReplace.keySet().each {
					if(newInstruction.contains(it)){
						newInstruction = newInstruction.replaceFirst(it, wordsToReplace.get(it))
					}
				}
				
				lines.add(newInstruction)
				
			} else {
				
				wordsToReplace.keySet().each {
					if(line.contains(it)){
						line = line.replaceFirst(it, wordsToReplace.get(it))
					}
				}
				
				lines.add(line)
			}
		}
		
		pathToCode = pathToCode.replace(".orcha", ".groovy")
		
		new File(pathToCode).withWriter('utf-8') { writer ->
			lines.each{
				writer.writeLine it
			}
		}
		
		log.info 'Conversion of the Orcha program to a Groovy program done successfully into :' + pathToCode
		
		return orchaFile.replace(".orcha", ".groovy")
		
	}
	
	private String instructionToReplace(String instruction){
		boolean instructionToReplace = false
		String newInstruction = null
		businessConfiguration.instruction.each { entry ->
			if(instructionToReplace==true && (entry.value instanceof java.util.HashMap)==false){
				newInstruction = entry.value
				return
			}
			if(entry.value instanceof java.util.HashMap){
				instructionToReplace = false
				def keys = entry.value.keySet()
				if(keys.size() == 1){	
					if(keys[0].equals(instruction)){						
						instructionToReplace = true
					}					
				}
			}			
		}
		return newInstruction
	}

}
