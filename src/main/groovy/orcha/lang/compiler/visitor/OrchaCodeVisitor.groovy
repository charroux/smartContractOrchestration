package orcha.lang.compiler.visitor

import java.util.List;
import java.util.Map;

import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.OrchaMetadata
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.configuration.Application;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport

/**
 * 
 * The Abstract Syntax Tree for Orcha generates a graph of Orcha instructions.
 * This graph is implemented with an adjacency graph of instructions.
 * 
 * @author Ben C.
 *
 */
abstract class OrchaCodeVisitor extends ClassCodeVisitorSupport{
	
	abstract OrchaMetadata getOrchaMetadata()
	
	//abstract void parseSourceFile(File orchaFile) throws OrchaCompilationException, OrchaConfigurationException
	
	/**
	 * 
	 * @return the adjacency graph containing all Orcha  instructions.
	 */
	abstract List<InstructionNode> findAllNodes()
	
	abstract InstructionNode findAdjacentNode(InstructionNode node)
	
	/**
	 * 
	 * @param node
	 * @return all adjacent instructions of the given node.
	 */
	abstract InstructionNode findNextRawNode(InstructionNode node)
	
	abstract List<InstructionNode> findNextNode(InstructionNode node)
	
	abstract InstructionNode findNextRawNode(InstructionNode node, List<InstructionNode> instructionsToExclude)
	
	abstract InstructionNode findNextRawNode(InstructionNode node, InstructionNode nodeToExclude)
	
	abstract List<InstructionNode> findAllReceiveNodes()
	
	abstract List<InstructionNode> findAllComputeNodes()
	
	abstract List<InstructionNode> findAllWhenNodes()
	
	/**
	 * compute sameAppli
	 * when "sameAppli terminates condition c1"
	 * ...
	 * when "sameAppli terminates condition c2"
	 * 
	 * @return
	 */
	abstract List<InstructionNode> findAllWhenNodesWithTheSameApplicationsInExpression()
	
	/**
	 * compute appli1
	 * when "appli1 terminates"
	 * ...
	 * compute appli2
	 * when "appli2 terminates condition c2"
	 * 
	 * @return
	 */
	abstract List<InstructionNode> findAllWhenNodesWithDifferentApplicationsInExpression()
	
	abstract List<InstructionNode> findAllWhenNodesWithManyApplicationsInExpression()
	
	//abstract boolean isDifferentNodesWithSameApplicationsInExpression(InstructionNode node1, InstructionNode node2)
	
	//abstract List<InstructionNode> findAllWhenNodesWithFailsAndTerminatesInExpression()
	
	//abstract boolean isFailsAndTerminatesInExpression(InstructionNode node)
	
	/**
	 * receive event from source condition c1
	 * ...
	 * receive event from source condition c2
	 * 
	 * @return
	 */
	abstract List<InstructionNode> findAllReceiveNodesWithTheSameEvent()
	
	abstract List<InstructionNode> findAllSendNodes()
	
	abstract List<InstructionNode> findAllPrecedingNodes(InstructionNode node)
	
	abstract List<InstructionNode> findAllRawPrecedingNodes(InstructionNode node)
	
	abstract Map<Class, List<InstructionNode>> findAllComputeNodesWithoutAdapterByConfigurationClass()
	
	abstract Map<Class, List<InstructionNode>> findAllNodesWithoutAdapterByConfigurationClass()
	
	/**
	 * The method to call at each node of the Orcha instructions tree shoul have 2 arguments: the next node, and the current node to be visited.
	 * 
	 * @param object the object on witch a method defined by methodName is called at each node of the tree
	 * @param methodName the name of the method to be called at each node of the tree
	 * @param node
	 * @param orchaCodeParser
	 * @param nodesAlreadyDone
	 */
	abstract void depthTraversal(def object, String methodName, InstructionNode node, OrchaCodeVisitor orchaCodeParser, List<InstructionNode> nodesAlreadyDone)
	
	//abstract List<InstructionNode> getGraphOfInstructions()
	
	//abstract Map<Instruction, List<Instruction>> getPredecedingInstructions()
	
}
