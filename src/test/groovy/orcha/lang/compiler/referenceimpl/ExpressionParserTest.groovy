package orcha.lang.compiler.referenceimpl

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest
class ExpressionParserTest {
	
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

}
