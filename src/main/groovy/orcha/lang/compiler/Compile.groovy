package orcha.lang.compiler

import orcha.lang.compiler.visitor.OrchaCodeParser

interface Compile{
	
	/**
	 * 
	 * @param orchaCodeParser
	 * @throws OrchaCompilationException
	 * @throws OrchaConfigurationException
	 */
	void compileForLaunching(OrchaCodeParser orchaCodeParser)  throws OrchaCompilationException, OrchaConfigurationException
	
	/**
	 * Deduce from the orchaCodeParser the test program (if it exists), and compile it.
	 * 
	 * @param orchaCodeParser
	 * @throws OrchaCompilationException
	 * @throws OrchaConfigurationException
	 */
	void compileForTesting(OrchaCodeParser orchaCodeParser)  throws OrchaCompilationException, OrchaConfigurationException
	
}
