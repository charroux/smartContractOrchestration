package orcha.lang.contract

import orcha.lang.compiler.visitor.OrchaCodeVisitor

interface ContractGenerator {
	
	String generate(OrchaCodeVisitor orchaCodeVisitor)
	
}
