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
	
	void updatePrerequisites(OrchaCodeVisitor orchaCodeVisitor)
	
	void updateCommitments(OrchaCodeVisitor orchaCodeVisitor)

	void updateProcess(OrchaCodeVisitor orchaCodeVisitor)
	
	void updateServiceLevelAgreements(OrchaCodeVisitor orchaCodeVisitor)
	
	void updateDeliveries(OrchaCodeVisitor orchaCodeVisitor)
	
	void exportToXML(File xmlFile)
	
	void exportToXML(File xmlFile, Format format)
	
}
