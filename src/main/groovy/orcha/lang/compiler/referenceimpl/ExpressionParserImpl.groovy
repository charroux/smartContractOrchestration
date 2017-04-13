package orcha.lang.compiler.referenceimpl

import java.util.List;

import orcha.lang.compiler.InstructionNode;
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.referenceimpl.ExpressionParser;
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.configuration.Application;

class ExpressionParserImpl implements ExpressionParser{
	
	@Override
	public String failChannel(InstructionNode computeNode, OrchaCodeParser orchaCodeParser){
		if(computeNode.instruction.instruction != "compute"){
			return null
		}
		List<InstructionNode> nextNodes = orchaCodeParser.findNextNode(computeNode)
		InstructionNode whenFailNode = nextNodes.find { this.isComputeFailsInExpression(computeNode, it.instruction.variable) == true }
		if(whenFailNode != null){
			String applicationName = computeNode.instruction.springBean.name
			return "error-channel-"+applicationName
		}
		return null
	}

	@Override
	public String failChannel(InstructionNode whenNode, List<InstructionNode> graphOfInstructions){
		boolean fails = this.isFailExpression(whenNode, graphOfInstructions)
		if(fails == false){
			return null
		}
		String orchaExpression = whenNode.instruction.variable
		String[] applicationNames = this.getApplicationsNamesInExpression(orchaExpression, graphOfInstructions)
		if(applicationNames.size() != 1){
			return null
		}
		return "error-channel-"+applicationNames[0]
	}

	@Override
	public String failedServiceName(InstructionNode whenNode, List<InstructionNode> graphOfInstructions){
		boolean fails = this.isFailExpression(whenNode, graphOfInstructions)
		if(fails == false){
			return null
		}
		String orchaExpression = whenNode.instruction.variable
		String[] applicationNames = this.getApplicationsNamesInExpression(orchaExpression, graphOfInstructions)
		if(applicationNames.size() != 1){
			return null
		}
		return applicationNames[0]
	}
	
	@Override
	public String releaseExpression(String expression, List<InstructionNode> graphOfInstructions) {
		
		List<String> applicationsNamesInExpression = this.getApplicationsNamesInExpression(expression, graphOfInstructions)
		
		String size = "size()=="  + applicationsNamesInExpression.size()
		
		int i=0
		def releaseExpression = ""
		Iterator names = applicationsNamesInExpression.iterator()	// is case of "a terminates and b terminates", names = [a, b]
		def name
		int index
		while(names.hasNext()){
			
			def expr = this.addParenthesis(expression, releaseExpression)
			expression = expr.get("expression")
			releaseExpression = expr.get("releaseExpression") 
			
			name = names.next()
			index = expression.indexOf(name) + name.length() + 1
			expression = expression.substring(index)
			expression = expression.trim()							// "terminates and b terminates"
			if(expression.startsWith("terminates")){				
				releaseExpression = releaseExpression + " ([" + i + "].payload instanceof T(orcha.lang.configuration.Application) AND [" + i + "].payload.state==T(orcha.lang.configuration.State).TERMINATED) "
				
				index = expression.indexOf("terminates") + "terminates".length()
				if(index >= expression.length()){
					releaseExpression = size + " and " + "(" + releaseExpression + ")"
					return releaseExpression
				}
				
				expression = expression.substring(index)
				expression = expression.trim()
				
				expr = this.addParenthesis(expression, releaseExpression)
				expression = expr.get("expression")
				releaseExpression = expr.get("releaseExpression") 
				
				if(expression.startsWith("and ")){					// "and b terminates"
					releaseExpression = releaseExpression + " and "
					index = expression.indexOf("and ") + "and".length() + 1
					expression = expression.substring(index)
					expression = expression.trim()
				} else if(expression.startsWith("or ")){
					releaseExpression = releaseExpression + " or "
					index = expression.indexOf("or ") + "or".length() + 1
					expression = expression.substring(index)
					expression = expression.trim()
				} else if(expression.startsWith("condition ")){
					
					releaseExpression = releaseExpression + " and "
					index = expression.indexOf("condition ") + "condition".length() + 1
					expression = expression.substring(index)
					expression = expression.trim()
					
					int endOfConditionIndex = this.endOfConditionIndex(expression)
					String condition = " [" + i + "].payload.output.value." + expression.substring(0, endOfConditionIndex) + " "
					
					releaseExpression = releaseExpression + condition
					
					expression = expression.substring(endOfConditionIndex)
					
				} 
				
				i++
				
			} 
			
		}
		
		
		
		releaseExpression = size + " and " + "(" + releaseExpression + ")"
		
		
		
		return releaseExpression
	}
	
	@Override
	public String aggregatorTransformerExpression(String expression, InstructionNode instructionNode, List<InstructionNode> graphOfInstructions) {
		
		List<String> applicationsNamesInExpression = this.getApplicationsNamesInExpression(expression, graphOfInstructions)
		
		String transformerExpression = "payload.?["
		
		String nextInstruction
			
		if(instructionNode.next.instruction.instruction == "send"){
		
			nextInstruction = instructionNode.next.instruction.variable
			
			if(applicationsNamesInExpression.contains(nextInstruction)){
				transformerExpression = transformerExpression + "name=='" + nextInstruction + "']"
			}
			
		} else if(instructionNode.next.instruction.instruction == "compute"){
		
			int manyWith = 1;
	
			def withs = instructionNode.next.instruction.withs
			withs.each{ with ->
				
				if(applicationsNamesInExpression.contains(with.with)){
					
					if(manyWith > 1){
						transformerExpression = transformerExpression + " or "
					}
					
					transformerExpression = transformerExpression + "name=='" + with.with + "'"
					
					manyWith++
				}
					
			}
				
			transformerExpression = transformerExpression + "]"
				
			
			return transformerExpression
		}
	}

	@Override
	public boolean isMultipleArgumentsInExpression(String expression, InstructionNode instructionNode, List<InstructionNode> graphOfInstructions) {
		
		List<String> applicationsNamesInExpression = this.getApplicationsNamesInExpression(expression, graphOfInstructions)
		int manyWith = 0
		
		String nextInstruction
			
		if(instructionNode.next.instruction.instruction == "send"){
		
			nextInstruction = instructionNode.next.instruction.variable
			manyWith = 1
			
		} else if(instructionNode.next.instruction.instruction == "compute"){
		
			def withs = instructionNode.next.instruction.withs
			withs.each{ with ->
				
				if(applicationsNamesInExpression.contains(with.with)){
					manyWith++
				}
					
			}
		}
		
		return manyWith > 1
	}
	
	private def addParenthesis(String expression, String releaseExpression){
		
		def returnedValue = [:]
		
		expression = expression.trim()
		
		while(expression.charAt(0) == ')'){
			releaseExpression = releaseExpression + ')'
			expression = expression.substring(1)
			expression = expression.trim()
			if(expression.length() == 0){
				returnedValue.put("expression", expression)
				returnedValue.put("releaseExpression", releaseExpression)
				return returnedValue
			}
		}
		
		while(expression.charAt(0) == '('){
			releaseExpression = releaseExpression + '('
			expression = expression.substring(1)
			expression = expression.trim()
		}
		
		returnedValue.put("expression", expression)
		returnedValue.put("releaseExpression", releaseExpression)

		return returnedValue

	}
	
	private int endOfConditionIndex(String expression){
		int index
		int min = expression.indexOf(')')
		
		index=expression.indexOf('and ')
		if(min>0 && index>0 && index<min){
			min = index
		}
		
		index=expression.indexOf('or ')
		if(min>0 && index>0 && index<min){
			min = index
		}
		
		if(min == -1){
			return expression.length()
		}
		
		return min
	}
	
	public List<String> getApplicationsNamesInExpression(String expression, List<InstructionNode> graphOfInstructions){
		def applicationsNames = []
		def applications = this.getApplicationsInExpression(expression, graphOfInstructions)
		applications.each{
			applicationsNames.add(it.name)
		}
		return applicationsNames	
	}
	
	public List<Application> getApplicationsInExpression(String expression, List<InstructionNode> graphOfInstructions){
		def applications = []
		String[] elements = expression.split(" ")
		Application application
		for(String element: elements){
			element = element.trim()
			
			while(element.charAt(0) == ')'){
				element = element.substring(1)
				element = element.trim()
			}
			
			while(element.charAt(0) == '('){
				element = element.substring(1)
				element = element.trim()
			}
			
			application = this.getByName(element, graphOfInstructions)
			if(application != null){
				applications.add(application)
			}
		}
		return applications
	}

	public List<Application> getTerminatedApplicationsInExpression(String expression, List<InstructionNode> graphOfInstructions){
		def applications = []
		String[] elements = expression.split(" ")
		Application application
		for(String element: elements){
			
			element = element.trim()
			
			if(element=="terminates" && application!=null){
				applications.add(application)
			}
			
			while(element.charAt(0) == ')'){
				element = element.substring(1)
				element = element.trim()
			}
			
			while(element.charAt(0) == '('){
				element = element.substring(1)
				element = element.trim()
			}
			
			application = this.getByName(element, graphOfInstructions)
			/*if(application != null){
				applications.add(application)
			}*/
		}
		return applications
	}

	public List<String> getTerminatedApplicationsNamesInExpression(String expression, List<InstructionNode> graphOfInstructions){
		def applicationsNames = []
		def applications = this.getTerminatedApplicationsInExpression(expression, graphOfInstructions)
		applications.each{
			applicationsNames.add(it.name)
		}
		return applicationsNames
	}
	
	private Application getByName(String applicationName, List<InstructionNode> graphOfInstructions){
		def application
		for(int i=0; i<graphOfInstructions.size(); i++){
			if(graphOfInstructions.get(i).instruction.springBean!=null && graphOfInstructions.get(i).instruction.springBean.name==applicationName){
				return graphOfInstructions.get(i).instruction.springBean
			}
		}
		return null
	}

	@Override
	public boolean isComputeFailsInExpression(InstructionNode instructionNode, String expression){
		
		String applicationName = instructionNode.instruction.springBean.name
		
		return this.isComputeFailsInExpression(applicationName, expression)

	}	

	@Override
	public boolean isComputeFailsInExpression(String applicationName, String expression) {

		expression = expression.trim()
		
		while(expression.charAt(0) == '('){
			expression = expression.substring(1)
			expression = expression.trim()
		}
		
		expression = expression.trim()
		
		String[] elements = expression.split(" ")
	
		if(elements[0].startsWith(applicationName) && elements[1] == "fails"){
			return true
		} else {
			return false
		}

	}

	@Override
	public boolean isComputeTerminatesInExpression( InstructionNode instructionNode, String expression) {
		
		String applicationName = instructionNode.instruction.springBean.name
		
		expression = expression.trim()
		
		while(expression.charAt(0) == '('){
			expression = expression.substring(1)
			expression = expression.trim()
		}
		
		expression = expression.trim()
		
		String[] elements = expression.split(" ")
		
		int index = elements.findIndexOf { it.startsWith(applicationName) }
		
		if(elements[index+1] == "terminates"){
			return true
		} else {
			return false
		}
	}

	@Override
	public boolean isComputeTerminatesInExpression(String applicationName, String expression) {
		
		expression = expression.trim()
		
		while(expression.charAt(0) == '('){
			expression = expression.substring(1)
			expression = expression.trim()
		}
		
		expression = expression.trim()
		
		String[] elements = expression.split(" ")
		
		int index = elements.findIndexOf { it.startsWith(applicationName) }
		
		if(elements[index+1] == "terminates"){
			return true
		} else {
			return false
		}
	}
	
	public boolean isFailExpression(InstructionNode instructionNode, List<InstructionNode> graphOfInstructions){
		
		if(instructionNode.options != null){
			if(instructionNode.options.failTest == true){
				return true
			}
		}
		
		String orchaExpression = instructionNode.instruction.variable
		List<String> applicationNames = getApplicationsNamesInExpression(orchaExpression, graphOfInstructions)
		if(applicationNames.size() != 1){
			return false
		}
		
		return isComputeFailsInExpression(applicationNames.getAt(0), orchaExpression)
	}

	public boolean isSeveralWhenWithSameApplicationsInExpression(InstructionNode instructionNode){
		
		if(instructionNode.instruction.instruction != "when"){
			return false
		}
		
		if(instructionNode.options != null){
			if(instructionNode.options.severalWhenWithSameApplicationsInExpression == true){
				return true
			}
		}
		
		return false
	}
}
