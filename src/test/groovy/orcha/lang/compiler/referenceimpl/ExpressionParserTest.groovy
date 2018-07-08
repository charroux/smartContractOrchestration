package orcha.lang.compiler.referenceimpl

import java.util.List

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor

@RunWith(SpringRunner.class)
@SpringBootTest
class ExpressionParserTest {
	
	@Autowired
	OrchaCodeParser orchaCodeParser
	
	@Test
	void releaseSpringLanguageExpressionTest(){
		
		ExpressionParserImpl expressionParser = new ExpressionParserImpl()
		
		String expression = "(codeToBenchmark1 terminates condition == -1) and (codeToBenchmark2 terminates condition == 1)"
				
		def applicationsNamesInExpression = ['codeToBenchmark1', 'codeToBenchmark2']
		String springExpression = expressionParser.releaseSpringLanguageExpression(expression, applicationsNamesInExpression)
		
		Assert.assertEquals(springExpression, "size()==2 and ( ( ([0].payload instanceof T(orcha.lang.configuration.Application) and [0].payload.state==T(orcha.lang.configuration.State).TERMINATED) and [0].payload.output.value== -1 ) and ( ([1].payload instanceof T(orcha.lang.configuration.Application) and [1].payload.state==T(orcha.lang.configuration.State).TERMINATED) and [1].payload.output.value== 1 ) )")
		
		
		expression = "(codeToBenchmark1 terminates condition == -1) or (codeToBenchmark2 terminates condition == 1)"
		
		springExpression = expressionParser.releaseSpringLanguageExpression(expression, applicationsNamesInExpression)
		
		Assert.assertEquals(springExpression, "size()==2 and ( ( ([0].payload instanceof T(orcha.lang.configuration.Application) and [0].payload.state==T(orcha.lang.configuration.State).TERMINATED) and [0].payload.output.value== -1 ) or ( ([1].payload instanceof T(orcha.lang.configuration.Application) and [1].payload.state==T(orcha.lang.configuration.State).TERMINATED) and [1].payload.output.value== 1 ) )")
		
	}
	
	@Test
	void sequenceNumberInExpression() {
		
		ExpressionParserImpl expressionParser = new ExpressionParserImpl()
		
		String orchaProgram = 	"title 'benchmarking services'\n" +
		"description 'Read a tesxt file, dispatch its content to two services, then launch the two services and wait until the two services complete. Write the result to a service into a file.'\n" +
		"receive event from benchmarkingInputFile\n" +
		"compute codeToBenchmark1 with event.value\n" +
		"receive event from benchmarkingInputFile\n" +
		"compute codeToBenchmark2 with event.value\n" +
		"when '(codeToBenchmark2 terminates condition == 1) and (codeToBenchmark1 terminates condition == -1)'\n" +
		"send codeToBenchmark1.result to benchmarkingOutputFile"
		
		// construct the graph of instructions for the Orcha programm
		
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		
		List<Instruction> graphOfInstructions = orchaCodeVisitor.findAllNodes()
		
		int number = expressionParser.getNumberOfApplicationsInExpression("(codeToBenchmark2 terminates condition == 1) and (codeToBenchmark1 terminates condition == -1)", graphOfInstructions)
		
		Assert.assertTrue(number == 2)
		
		number = expressionParser.getNumberOfApplicationsInExpression("azerty", graphOfInstructions)
		
		Assert.assertTrue(number == 0)
		
		int index = expressionParser.getIndexOfApplicationInExpression("(codeToBenchmark2 terminates condition == 1) and (codeToBenchmark1 terminates condition == -1)", "codeToBenchmark2", graphOfInstructions)
		
		Assert.assertTrue(index == 1)
		
		index = expressionParser.getIndexOfApplicationInExpression("(codeToBenchmark2 terminates condition == 1) and (codeToBenchmark1 terminates condition == -1)", "codeToBenchmark1", graphOfInstructions)
		
		Assert.assertTrue(index == 2)
		
		index = expressionParser.getIndexOfApplicationInExpression("(codeToBenchmark2 terminates condition == 1) and (codeToBenchmark1 terminates condition == -1)", "azerty", graphOfInstructions)
		
		Assert.assertTrue(index == 0)
		
		index = expressionParser.getIndexOfApplicationInExpression("azerty", "codeToBenchmark1", graphOfInstructions)
		
		Assert.assertTrue(index == -1)
		
	}

}
