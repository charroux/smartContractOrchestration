package orcha.lang.compiler.visitor

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.compiler.visitor.impl.OrchaCodeParserImpl
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest
class OrchaCodeParserTest {
	
	@Autowired
	OrchaCodeParser orchaCodeParser
	
	String expression = '"simpleApplicationService terminates condition ==true"'
	
	@Test
	void orchaCodeParserSimpleApplicationTest(){
		
		String orchaProgram = 	"package source.simpleTest\n" +
								"title 'simple application to test'\n" +
								"receive variable from simpleApplicationInput\n" +						// instruction 1
								"compute simpleApplicationService with variable.value\n" +				// instruction 2
								"when " + expression + "\n" +											// instruction 3
								"send simpleApplicationService.result to simpleApplicationOutput"		// instruction 4
								
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)		
		Assert.assertTrue(orchaCodeVisitor.getOrchaMetadata().getTitle() == "simple application to test")

		def receiveNodes = orchaCodeVisitor.findAllReceiveNodes()
		Assert.assertTrue(receiveNodes.size() == 1)
		Assert.assertTrue(receiveNodes[0] instanceof InstructionNode)
		InstructionNode receive = (InstructionNode)receiveNodes[0]
		Assert.assertTrue(receive.instruction.instruction == 'receive')
		Assert.assertTrue(receive.instruction.id == 1)
		Assert.assertEquals(receive.instruction.variable, 'variable')
		Assert.assertNull(receive.instruction.variableProperty)
		Assert.assertTrue(receive.instruction.withs.size() == 0)
		Assert.assertTrue(receive.instruction.springBean instanceof EventHandler)
		EventHandler receiveEventHandler = (EventHandler)receive.instruction.springBean
		Assert.assertEquals(receiveEventHandler.name, 'simpleApplicationInput')
		Assert.assertNotNull(receiveEventHandler.input)
		Assert.assertNull(receive.instruction.condition)
		
		def computeNodes = orchaCodeVisitor.findAllComputeNodes()
		Assert.assertTrue(computeNodes.size() == 1)
		Assert.assertTrue(computeNodes[0] instanceof InstructionNode)
		InstructionNode compute = (InstructionNode)computeNodes[0]
		Assert.assertTrue(compute.instruction.instruction == 'compute')
		Assert.assertTrue(compute.instruction.id == 2)
		Assert.assertEquals(compute.instruction.variable, 'simpleApplicationService')
		Assert.assertNull(compute.instruction.variableProperty)
		Assert.assertTrue(compute.instruction.withs.size() == 1)
		Assert.assertEquals(compute.instruction.withs[0].with, 'variable')
		Assert.assertEquals(compute.instruction.withs[0].withProperty, 'value')
		Assert.assertTrue(compute.instruction.springBean instanceof Application)
		Application computeApplication = (Application)compute.instruction.springBean
		Assert.assertEquals(computeApplication.name, 'simpleApplicationService')
		Assert.assertNull(compute.instruction.condition)
		
		//InstructionNode adjacentToReceive = orchaCodeVisitor.findAdjacentNode(receive)
		//Assert.assertEquals(adjacentToReceive.instruction, compute.instruction)
		
		def whenNodes = orchaCodeVisitor.findAllWhenNodes()
		Assert.assertTrue(whenNodes.size() == 1)
		Assert.assertTrue(whenNodes[0] instanceof InstructionNode)
		InstructionNode when = (InstructionNode)whenNodes[0]
		Assert.assertTrue(when.instruction.instruction == 'when')
		Assert.assertTrue(when.instruction.id == 3)
		Assert.assertTrue(when.instruction.withs.size() == 0)
		Assert.assertNull(when.instruction.condition)
		
		def precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(when)
		Assert.assertTrue(precedingNodes.size() == 1)
		InstructionNode precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'compute')
		Assert.assertTrue(precedingNode.instruction.id == 2)
		
		
		def sendNodes = orchaCodeVisitor.findAllSendNodes()
		Assert.assertTrue(sendNodes.size() == 1)
		Assert.assertTrue(sendNodes[0] instanceof InstructionNode)
		InstructionNode send = (InstructionNode)sendNodes[0]
		Assert.assertTrue(send.instruction.instruction == 'send')
		Assert.assertTrue(send.instruction.id == 4)
		Assert.assertEquals(send.instruction.variable, 'simpleApplicationService')
		Assert.assertEquals(send.instruction.variableProperty, 'result')
		Assert.assertTrue(send.instruction.withs.size() == 0)
		Assert.assertTrue(send.instruction.springBean instanceof EventHandler)
		EventHandler sendEventHandler = (EventHandler)send.instruction.springBean
		Assert.assertEquals(sendEventHandler.name, 'simpleApplicationOutput')
		Assert.assertNotNull(sendEventHandler.output)
		Assert.assertNull(send.instruction.condition)
	}

	@Test
	void orchaCodeParserOrchaCompilerTest(){
		
		String orchaProgram = 	"package source.orcha\n" +
								"domain orcha\n" +
								"description 'orcha compiler'\n" +
								"title 'orcha compiler'\n" +
								"author 'Ben C.'\n" +
								"version '1.0'\n" +
								"receive orchaProgram from orchaFile\n" +							// instruction 1
								"compute parseOrcha with orchaProgram.value\n" +					// instruction 2
								"when 'parseOrcha terminates'\n" +									// instruction 3
								"compute generateServiceOfferSelection with parseOrcha.result\n" +	// instruction 4
								"when 'parseOrcha terminates'\n" +									// instruction 5
								"compute generateServiceOfferSelection with parseOrcha.result"		// instruction 6		
								
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		Assert.assertEquals(orchaCodeVisitor.getOrchaMetadata().getDomain(), "orcha")
		Assert.assertEquals(orchaCodeVisitor.getOrchaMetadata().getDescription(), "orcha compiler")
		Assert.assertEquals(orchaCodeVisitor.getOrchaMetadata().getAuthor(), "Ben C.")
		Assert.assertEquals(orchaCodeVisitor.getOrchaMetadata().getVersion(), "1.0")
		
		def allNodes = orchaCodeVisitor.findAllNodes()
		Assert.assertTrue(allNodes.size() > 0)
		InstructionNode node = allNodes.get(0)
		Assert.assertTrue(node.instruction.instruction == 'receive')
		Assert.assertTrue(node.instruction.id == 1)
		
		def nextNodes = orchaCodeVisitor.findNextNode(node)
		Assert.assertTrue(nextNodes.size() == 1)
		node = nextNodes.get(0)
		Assert.assertTrue(node.instruction.instruction == 'compute')
		Assert.assertTrue(node.instruction.id == 2)
		
		def precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(node)	
		Assert.assertTrue(precedingNodes.size() == 1)
		InstructionNode precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'receive')
		Assert.assertTrue(precedingNode.instruction.id == 1)
		
		
		nextNodes = orchaCodeVisitor.findNextNode(node)
		Assert.assertTrue(nextNodes.size() == 3)
		InstructionNode whenNode1 = nextNodes.get(1)		// nextNodes.get(0) is a generic when node
		Assert.assertTrue(whenNode1.instruction.instruction == 'when')
		Assert.assertTrue(whenNode1.instruction.id == 3)
		
		precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(whenNode1)
		Assert.assertTrue(precedingNodes.size() == 1)
		precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'compute')
		Assert.assertTrue(precedingNode.instruction.id == 2)
		
		
		InstructionNode whenNode2 = nextNodes.get(2)
		Assert.assertTrue(whenNode2.instruction.instruction == 'when')
		Assert.assertTrue(whenNode2.instruction.id == 5)
		
		precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(whenNode2)
		Assert.assertTrue(precedingNodes.size() == 1)
		precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'compute')
		Assert.assertTrue(precedingNode.instruction.id == 2)
		
		
		nextNodes = orchaCodeVisitor.findNextNode(whenNode1)
		Assert.assertTrue(nextNodes.size() == 1)
		node = nextNodes.get(0)
		Assert.assertTrue(node.instruction.instruction == 'compute')
		Assert.assertTrue(node.instruction.id == 4)
		
		precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(node)
		Assert.assertTrue(precedingNodes.size() == 1)
		precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'when')
		Assert.assertTrue(precedingNode.instruction.id == 3)
		
		
		nextNodes = orchaCodeVisitor.findNextNode(whenNode2)
		Assert.assertTrue(nextNodes.size() == 1)
		node = nextNodes.get(0)
		Assert.assertTrue(node.instruction.instruction == 'compute')
		Assert.assertTrue(node.instruction.id == 6)

		precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(node)
		Assert.assertTrue(precedingNodes.size() == 1)
		precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'when')
		Assert.assertTrue(precedingNode.instruction.id == 5)
		
		
		def whenNodes = orchaCodeVisitor.findAllWhenNodes()
		Assert.assertTrue(whenNodes.size() == 3)				// one fictive node + the two when instructions
		InstructionNode when = (InstructionNode)whenNodes[1]
		Assert.assertTrue(when.instruction.instruction == 'when')
		Assert.assertTrue(when.instruction.id == 3)
		
		when = (InstructionNode)whenNodes[2]
		Assert.assertTrue(when.instruction.instruction == 'when')
		Assert.assertTrue(when.instruction.id == 5)

	}

	@Test
	void orchaCodeParserOrchaBenchmarkingVendorTest(){
		
		String orchaProgram = 	"package source.order\n" +
								"domain productSales\n" +
								"description 'sales TV'\n" +
								"title 'select best TV vendors'\n" +
								"author 'Ben C.'\n" +
								"version '1.0'\n" +
								"receive order from customer condition \"order.product.specification == 'TV'\"\n" +	// instruction 1
								"compute orderConverter with order.value\n" +										// instruction 2
								"when 'orderConverter terminates'\n" +												// instruction 3
								"compute vendor1 with orderConverter.result\n" +									// instruction 4
								"receive order from customer condition \"order.product.specification == 'TV'\"\n" +	// instruction 5
								"compute vendor2 with order.value\n" +												// instruction 6
								"receive order from customer condition \"order.product.specification == 'TV'\"\n" +	// instruction 7
								"compute vendor3 with order.value\n" +												// instruction 8
								"when '(vendor1 terminates) and (vendor2 terminates) and (vendor3 terminates)'\n" +	// instruction 9
								"compute selectBestVendor with vendor1.result, vendor2.result, vendor3.result\n" +	// instruction 10			
								"when  'selectBestVendor terminates condition price>1000'\n" +						// instruction 11
								"send selectBestVendor.result to outputFile1\n" +									// instruction 12
								"when 'selectBestVendor terminates condition price<=1000'\n" + 						// instruction 13
								"send selectBestVendor.result to outputFile2"										// instruction 14
								
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		
		def allReceiveNodes = orchaCodeVisitor.findAllReceiveNodes()
		Assert.assertTrue(allReceiveNodes.size() == 4)
		InstructionNode node = allReceiveNodes.get(1)
		Assert.assertTrue(node.instruction.instruction == 'receive')
		Assert.assertTrue(node.instruction.id == 1)
		
		def nextNodes = orchaCodeVisitor.findNextNode(node)
		Assert.assertTrue(nextNodes.size() == 1)
		node = nextNodes.get(0)
		Assert.assertTrue(node.instruction.instruction == 'compute')
		Assert.assertTrue(node.instruction.id == 2)
		
		def precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(node)
		Assert.assertTrue(precedingNodes.size() == 1)
		InstructionNode precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'receive')
		Assert.assertTrue(precedingNode.instruction.id == 1)
		
		
		node = allReceiveNodes.get(2)
		Assert.assertTrue(node.instruction.instruction == 'receive')
		Assert.assertTrue(node.instruction.id == 5)
		
		nextNodes = orchaCodeVisitor.findNextNode(node)
		Assert.assertTrue(nextNodes.size() == 1)
		node = nextNodes.get(0)
		Assert.assertTrue(node.instruction.instruction == 'compute')
		Assert.assertTrue(node.instruction.id == 6)
		
		precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(node)
		Assert.assertTrue(precedingNodes.size() == 1)
		precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'receive')
		Assert.assertTrue(precedingNode.instruction.id == 5)
		
		
		node = allReceiveNodes.get(3)
		Assert.assertTrue(node.instruction.instruction == 'receive')
		Assert.assertTrue(node.instruction.id == 7)
		
		nextNodes = orchaCodeVisitor.findNextNode(node)
		Assert.assertTrue(nextNodes.size() == 1)
		node = nextNodes.get(0)
		Assert.assertTrue(node.instruction.instruction == 'compute')
		Assert.assertTrue(node.instruction.id == 8)
		
		precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(node)
		Assert.assertTrue(precedingNodes.size() == 1)
		precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'receive')
		Assert.assertTrue(precedingNode.instruction.id == 7)
		
		
		def whenNodes = orchaCodeVisitor.findAllWhenNodes()
		Assert.assertTrue(whenNodes.size() == 5)
		Assert.assertTrue(whenNodes[0] instanceof InstructionNode)
		InstructionNode when = (InstructionNode)whenNodes[3]
		Assert.assertTrue(when.instruction.instruction == 'when')
		Assert.assertTrue(when.instruction.id == 11)
		//Assert.assertEquals(when.instruction.condition, 'price<=1000')

	}


}
