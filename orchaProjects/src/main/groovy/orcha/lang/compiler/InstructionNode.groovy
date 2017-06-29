package orcha.lang.compiler

import groovy.transform.ToString;

/**
 * Add information to an instruction so a graph of InstructionNode is built
 * 
 * @author Ben C.
 *
 */
@ToString
class InstructionNode {
	
	Instruction instruction
	String inputName			// name of the previous instruction node
	String outputName			// name of the next instruction node
	
	def options					// free field
	
	InstructionNode next

}
