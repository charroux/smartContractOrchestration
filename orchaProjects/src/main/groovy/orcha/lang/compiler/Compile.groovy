package orcha.lang.compiler

import java.util.List;
import java.util.Map;
import orcha.lang.compiler.visitor.OrchaCodeParser

interface Compile{
	
	/**
	 * 
	 * @param orchaCodeParser
	 * @throws OrchaCompilationException
	 * @throws OrchaConfigurationException
	 */
	void compile(OrchaCodeParser orchaCodeParser)  throws OrchaCompilationException, OrchaConfigurationException
	
}
