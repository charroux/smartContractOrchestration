package orcha.lang.compiler.visitor

import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.compiler.visitor.impl.OrchaCodeParserImpl
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
	
	@Test
	void orchaCodeParserTest(){
		
		String orchaProgram = 	"package source.simpleTest\n" +
								"title 'simple application to test'\n" +
								"receive variable from simpleApplicationInput\n" +
								"compute simpleApplicationService with variable.value\n" +
								"when 'simpleApplicationService terminates with result!=true'\n" +
								"send simpleApplicationService.result to simpleApplicationOutput"
								
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		
		Assert.assertTrue(orchaCodeVisitor.getOrchaMetadata().getTitle() == "simple application to test")
		
	}


}
