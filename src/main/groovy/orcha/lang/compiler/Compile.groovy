package orcha.lang.compiler

import java.io.File

import orcha.lang.compiler.visitor.OrchaCodeVisitor

interface Compile{
	
	/**
	 * 
	 * @param orchaCodeParser
	 * @throws OrchaCompilationException
	 * @throws OrchaConfigurationException
	 */
	void compileForLaunching(OrchaCodeVisitor orchaCodeParser)  throws OrchaCompilationException, OrchaConfigurationException
	
	/**
	 * Deduce from the orchaCodeParser the test program (if it exists), and compile it.
	 * 
	 * @param orchaCodeParser
	 * @throws OrchaCompilationException
	 * @throws OrchaConfigurationException
	 */
	void compileForTesting(OrchaCodeVisitor orchaCodeParser)  throws OrchaCompilationException, OrchaConfigurationException
	
	void compile(OrchaCodeVisitor orchaCodeParser, File resourcesDestinationDirectory, File binaryCodeDirectory) throws OrchaCompilationException, OrchaConfigurationException
	
}
