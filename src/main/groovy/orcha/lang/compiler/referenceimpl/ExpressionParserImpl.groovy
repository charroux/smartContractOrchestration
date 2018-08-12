package orcha.lang.compiler.referenceimpl

import java.util.List;

import orcha.lang.compiler.InstructionNode;
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.referenceimpl.ExpressionParser;
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.configuration.Application;
import org.springframework.expression.spel.SpelParseException
import org.springframework.expression.spel.standard.SpelExpression
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.SpelMessage

class ExpressionParserImpl implements ExpressionParser{
	
	OrchaCodeVisitor orchaCodeVisitor
	
	public OrchaCodeVisitor getOrchaCodeVisitor() {
		return orchaCodeVisitor;
	}

	public void setOrchaCodeVisitor(OrchaCodeVisitor orchaCodeVisitor) {
		this.orchaCodeVisitor = orchaCodeVisitor;
	}

	@Override
	public String failChannel(InstructionNode computeNode){
		if(computeNode.instruction.instruction != "compute"){
			return null
		}
		List<InstructionNode> nextNodes = orchaCodeVisitor.findNextNode(computeNode)
		InstructionNode whenFailNode = nextNodes.find { this.isComputeFailsInExpression(computeNode, it.instruction.variable) == true }
		if(whenFailNode != null){
			String applicationName = computeNode.instruction.springBean.name
			return "error-channel-"+applicationName
		}
		return null
	}

	/*@Override
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
	}*/

	@Override
	public String failedServiceName(InstructionNode whenNode){
		List<InstructionNode> graphOfInstructions = orchaCodeVisitor.findAllNodes()
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

	/**
	 * Add payload before variable.
	 * Exp. : 
	 * 		[(specification == 'TV' or order.price == 30) and == 0] and == 5
	 * becomes :
	 *		[(payload.specification == 'TV' or payload.order.price == 30) and payload == 0] and payload == 5 
	 */
	@Override
	public String filteringExpression(String expression) {
		
		String tmp = expression.trim()
		String filterExpression = expression
		int indexOfVariable = 0
		
		while(tmp.isEmpty() == false) {

			//skip [, ( and white space
			int index = 0
			while(tmp.getAt(index).equals("(") || tmp.getAt(index).equals("[")) {
				index++
			}
			
			tmp = tmp.substring(index).trim()
			
			indexOfVariable = indexOfVariable + index
			
			// if tmp = '== value' => no . to add after payload: tmp = payload == value
			// if tmp = 'x == value' => add . after payload: tmp = payload.x == value
			if(tmp.startsWith("==") || tmp.startsWith("<=") || tmp.startsWith(">=")) {
				// find variable after an operator
				//tmp = tmp.substring(2).trim()
				filterExpression = filterExpression.substring(0, indexOfVariable) + "payload " + filterExpression.substring(indexOfVariable)
				indexOfVariable = indexOfVariable + 8
			} else if(tmp.startsWith("<") || tmp.startsWith(">")){
				// find variable after an operator
				//tmp = tmp.substring(1).trim()
			} else {
				filterExpression = filterExpression.substring(0, indexOfVariable) + "payload." + filterExpression.substring(indexOfVariable)
				indexOfVariable = indexOfVariable + 8
			}
			
			// look for next variable (after an and)
			int indexOfAnd = tmp.indexOf(" and ")
			int indexOfOr = tmp.indexOf(" or ")
			int indexOfNot = tmp.indexOf(" not ")
			
			if(indexOfAnd == -1) {
				indexOfAnd = Integer.MAX_VALUE
			}
			if(indexOfOr == -1) {
				indexOfOr = Integer.MAX_VALUE
			}
			if(indexOfNot == -1) {
				indexOfNot = Integer.MAX_VALUE
			}
			
			if(indexOfAnd < indexOfOr){
				if(indexOfAnd < indexOfNot){	// min indexOfAnd
					tmp = tmp.substring(indexOfAnd+5).trim()
					indexOfVariable = indexOfVariable + indexOfAnd + 5
				} else { 						// min indexOfNot
					tmp = tmp.substring(indexOfNot+5).trim()
					indexOfVariable = indexOfVariable + indexOfNot + 5
				}
			} else {	// min indexOfOr or (indexOfAnd=Integer.MAX_VALUE and indexOfOr=Integer.MAX_VALUE) 
				if(indexOfOr < indexOfNot){	// min indexOfOr
					tmp = tmp.substring(indexOfOr+4).trim()
					indexOfVariable = indexOfVariable + indexOfOr + 4
				} else if(indexOfNot != Integer.MAX_VALUE){ 		// min indexOfNot
					tmp = tmp.substring(indexOfNot+5).trim()
					indexOfVariable = indexOfVariable + indexOfNot + 5
				} else {
					tmp = tmp.substring(tmp.length())
				}
			}
		
		}
		
		return filterExpression
			
	}
	
	private String releaseSpringLanguageExpression(String expression, List<String> applicationsNamesInExpression) {
		
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
			
			if(expression.startsWith("and ")){					// "and (codeToBenchmark2 terminates condition == 1)"
				releaseExpression = releaseExpression + " and ("
				index = expression.indexOf("and ") + "and".length() + 1
				expression = expression.substring(index)
				expression = expression.trim()
			} else if(expression.startsWith("or ")){
				releaseExpression = releaseExpression + " or ("
				index = expression.indexOf("or ") + "or".length() + 1
				expression = expression.substring(index)
				expression = expression.trim()
			}
			
			index = expression.indexOf(name) + name.length() + 1
			expression = expression.substring(index)
			expression = expression.trim()							// "terminates and b terminates"
			if(expression.startsWith("terminates")){
				releaseExpression = releaseExpression + " (messages[" + i + "].payload instanceof T(orcha.lang.configuration.Application) and messages[" + i + "].payload.state==T(orcha.lang.configuration.State).TERMINATED) "
				
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
					
					releaseExpression = releaseExpression + "and "
					index = expression.indexOf("condition ") + "condition".length() + 1
					expression = expression.substring(index)
					expression = expression.trim()
					
					int endOfConditionIndex = this.endOfConditionIndex(expression)
					
					boolean leftOperatorInExpression = this.leftOperatorInExpression(expression)
					
					String condition
					
					if(leftOperatorInExpression == true){
						condition = "messages[" + i + "].payload.output.value." + expression.substring(0, endOfConditionIndex) + " "
					} else {
						condition = "messages[" + i + "].payload.output.value" + expression.substring(0, endOfConditionIndex) + " "
					}
					
					
					releaseExpression = releaseExpression + condition
					
					expression = expression.substring(endOfConditionIndex)
					
				}
				
				i++
				
			}
			
		}
		
		releaseExpression = size + " and " + "( " + releaseExpression
		
		if(applicationsNamesInExpression.size() > 1) {
			releaseExpression = releaseExpression + ")"
		}
		
		releaseExpression = releaseExpression + " )"
		
		return releaseExpression

	}
	
	@Override
	public String releaseExpression(String expression) {
		
		List<String> applicationsNamesInExpression = this.getApplicationsNamesInExpression(expression)
		
		return this.releaseSpringLanguageExpression(expression, applicationsNamesInExpression)
		
	}
	
	@Override
	public String aggregatorTransformerExpression(String expression, InstructionNode instructionNode) {
		
		List<String> applicationsNamesInExpression = this.getApplicationsNamesInExpression(expression)
		
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
	public boolean isMultipleArgumentsInExpression(String expression, InstructionNode instructionNode) {
		
		List<String> applicationsNamesInExpression = this.getApplicationsNamesInExpression(expression)
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

	/**
	 * For expressions like this: when " ... condition <0"
	 * there is no left operator
	 *  
	 * @param expression
	 * @return true if there is a left operator, false otherwise
	 */
	private boolean leftOperatorInExpression(String expression){
		
		try{
			SpelExpressionParser parser = new SpelExpressionParser();
			SpelExpression se =	parser.parseRaw(expression)
		}catch(SpelParseException e){
			if(e.getMessageCode() == SpelMessage.LEFT_OPERAND_PROBLEM){
				return false
			}
		}
		
		return true
		
	}
	
	public List<String> getApplicationsNamesInExpression(String expression){
		def applicationsNames = []
		def applications = this.getApplicationsInExpression(expression)
		applications.each{
			applicationsNames.add(it.name)
		}
		return applicationsNames	
	}
	
	public List<Application> getApplicationsInExpression(String expression){
		
		List<InstructionNode> graphOfInstructions = orchaCodeVisitor.findAllNodes()
		
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

	public List<Application> getTerminatedApplicationsInExpression(String expression){
		
		List<InstructionNode> graphOfInstructions = orchaCodeVisitor.findAllNodes()
		
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

	public List<String> getTerminatedApplicationsNamesInExpression(String expression){
		def applicationsNames = []
		def applications = this.getTerminatedApplicationsInExpression(expression)
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
	
	public boolean isFailExpression(InstructionNode instructionNode){
		
		if(instructionNode.options != null){
			if(instructionNode.options.failTest == true){
				return true
			}
		}
		
		String orchaExpression = instructionNode.instruction.variable
		List<String> applicationNames = getApplicationsNamesInExpression(orchaExpression)
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

	@Override
	public int getNumberOfApplicationsInExpression(String expression) {
		List<String> applicationsNames = this.getApplicationsNamesInExpression(expression)
		return applicationsNames.size()
	}

	@Override
	public int getIndexOfApplicationInExpression(String expression, String applicationName) {
		List<String> applicationsNames = this.getApplicationsNamesInExpression(expression)
		if(applicationsNames.size() == 0) {
			return -1
		}
		int sequenceNumber = applicationsNames.findIndexOf{ it == applicationName }
		sequenceNumber++
		return sequenceNumber
	}

	
}
