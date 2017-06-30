package orcha.lang.compiler.visitor

import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.OrchaConfigurationException

interface OrchaCodeParser {
	
	OrchaCodeVisitor parse(File orchaFile) throws OrchaCompilationException, OrchaConfigurationException
	
	OrchaCodeVisitor parse(String orchaProgram) throws OrchaCompilationException, OrchaConfigurationException

}
