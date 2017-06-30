package orcha.lang.compiler.referenceimpl.xmlgenerator

import java.util.List;
import java.util.Map;
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.visitor.OrchaCodeVisitor

interface XmlGenerator {
	
	void generate(OrchaCodeVisitor orchaCodeParser, File xmlSpringContextFile, File xmlQoSSpringContextFile)

}
