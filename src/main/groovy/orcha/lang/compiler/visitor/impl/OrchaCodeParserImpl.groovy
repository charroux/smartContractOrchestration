package orcha.lang.compiler.visitor.impl

import java.io.File

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

import groovy.util.logging.Slf4j
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.visitor.MyClassLoader
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor

@Slf4j
class OrchaCodeParserImpl implements OrchaCodeParser{
	
	@Autowired
	ApplicationContext context
	
	@Autowired
	ExpressionParser expressionParser
	
	@Override
	public OrchaCodeVisitor parse(File orchaFile) throws OrchaCompilationException, OrchaConfigurationException {
	
		OrchaCodeVisitorImpl orchaCodeVisitor = new OrchaCodeVisitorImpl(context: context, expressionParser: expressionParser)
		
		try{
			
			def myCL = new MyClassLoader(visitor: orchaCodeVisitor)
			
			try{
	
				def script = myCL.parseClass(new GroovyCodeSource(orchaFile))
				
				orchaCodeVisitor.getGraphOfInstructions()
				
			} catch(org.codehaus.groovy.control.MultipleCompilationErrorsException e){
				Iterator errors = e.getErrorCollector().getErrors().iterator()
				while(errors.hasNext()){
					throw errors.next().getCause()
				}
			}
		}catch(org.springframework.beans.factory.NoSuchBeanDefinitionException e){
			String message = "Orcha configuration error while parsing the orcha file (" + orchaFile.getAbsolutePath() + "): no definition for "  + e.getBeanName() + ". Please define it as a bean in an Oche configuration class."
			log.error "Orcha configuration error while parsing the orcha file (" + orchaFile.getAbsolutePath() + "): no definition for "  + e.getBeanName() + ". Please define it as a bean in an Oche configuration class."
			throw new OrchaConfigurationException(message)
		}catch(Exception e){
			log.error e.getMessage()
			throw e
		}

		return orchaCodeVisitor
	}
	

}
