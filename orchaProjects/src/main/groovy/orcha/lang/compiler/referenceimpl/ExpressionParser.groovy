package orcha.lang.compiler.referenceimpl

interface ExpressionParser {
	
	String releaseExpression(String expression, List<InstructionNode> graphOfInstructions)

}