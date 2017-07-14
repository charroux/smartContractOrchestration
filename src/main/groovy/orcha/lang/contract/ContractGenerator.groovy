package orcha.lang.contract

import java.io.File

import orcha.lang.compiler.visitor.OrchaCodeVisitor

interface ContractGenerator {
	
	enum Format{
		PrettyFormat,
		CompactFormat,
		RawFormat 
	}
	
	void generateAll(OrchaCodeVisitor orchaCodeVisitor)
	
	void updateCommitments(OrchaCodeVisitor orchaCodeVisitor)

	void updateRequirements(OrchaCodeVisitor orchaCodeVisitor)
	
	void updateQualityOfServices(OrchaCodeVisitor orchaCodeVisitor)
	
	void exportToXML(File xmlFile)
	
	void exportToXML(File xmlFile, Format format)
	
}
