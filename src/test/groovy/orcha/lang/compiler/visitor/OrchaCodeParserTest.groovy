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
	
	String expression = '"simpleApplicationService terminates with !=true"'
	
	@Test
	void orchaCodeParserTest1(){
		
		String orchaProgram = 	"package source.simpleTest\n" +
								"title 'simple application to test'\n" +
								"receive variable from simpleApplicationInput\n" +
								"compute simpleApplicationService with variable.value\n" +
								"when " + expression + "\n" +
								"send simpleApplicationService.result to simpleApplicationOutput"
								
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
	void orchaCodeParserTest2(){
		
		String orchaProgram = 	"package source.orcha\n" +
								"domain orcha\n" +
								"description 'orcha compiler'\n" +
								"title 'orcha compiler'\n" +
								"author 'Ben C.'\n" +
								"version '1.0'\n" +
								"receive orchaProgram from orchaFile\n" +
								"compute parseOrcha with orchaProgram.value\n" +
								"when 'parseOrcha terminates'\n" +
								"compute generateServiceOfferSelection with parseOrcha.result\n" +
								"when 'parseOrcha terminates'\n" +
								"compute generateServiceOfferSelection with parseOrcha.result"								
								
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
		Assert.assertTrue(nextNodes.size() == 2)
		InstructionNode whenNode1 = nextNodes.get(0)
		Assert.assertTrue(whenNode1.instruction.instruction == 'when')
		Assert.assertTrue(whenNode1.instruction.id == 3)
		
		precedingNodes = orchaCodeVisitor.findAllPrecedingNodes(whenNode1)
		Assert.assertTrue(precedingNodes.size() == 1)
		precedingNode = precedingNodes.get(0)
		Assert.assertTrue(precedingNode.instruction.instruction == 'compute')
		Assert.assertTrue(precedingNode.instruction.id == 2)
		
		
		InstructionNode whenNode2 = nextNodes.get(1)
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

	}


}
