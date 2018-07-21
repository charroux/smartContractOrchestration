package orcha.lang.compiler.visitor.impl

import java.io.File

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

import groovy.util.logging.Slf4j
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.visitor.MyClassLoader
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor

//@Slf4j
class OrchaCodeParserImpl implements OrchaCodeParser{
	
	private static Logger log = LoggerFactory.getLogger(OrchaCodeVisitorImpl.class);
	
	@Autowired
	ApplicationContext context
	
	@Autowired
	ExpressionParser expressionParser
	
	@Override
	public OrchaCodeVisitor parse(String orchaProgram) throws OrchaCompilationException, OrchaConfigurationException{

		OrchaCodeVisitorImpl orchaCodeVisitor = new OrchaCodeVisitorImpl(orchaSourceProgram: orchaProgram, context: context, expressionParser: expressionParser)
		
		expressionParser.setOrchaCodeVisitor(orchaCodeVisitor)
		
		try{
			
			def myCL = new MyClassLoader(visitor: orchaCodeVisitor)
			
			try{
				
				def script = myCL.parseClass(orchaProgram)
				
				orchaCodeVisitor.getGraphOfInstructions()
				
			} catch(org.codehaus.groovy.control.MultipleCompilationErrorsException e){
				Iterator errors = e.getErrorCollector().getErrors().iterator()
				while(errors.hasNext()){
					throw errors.next().getCause()
				}
			}
		}catch(org.springframework.beans.factory.NoSuchBeanDefinitionException e){
			String message = "Orcha configuration error while parsing the orcha program: no definition for "  + e.getBeanName() + ". Please define it as a bean in an Oche configuration class."
			log.error "Orcha configuration error while parsing the orcha program: no definition for "  + e.getBeanName() + ". Please define it as a bean in an Oche configuration class."
			throw new OrchaConfigurationException(message)
		}catch(Exception e){
			log.error e.getMessage()
			throw e
		}

		return orchaCodeVisitor

	}
	
	@Override
	public OrchaCodeVisitor parse(File orchaFile) throws OrchaCompilationException, OrchaConfigurationException {
	
		String orchaProgram = new String(orchaFile.bytes)
		
		return this.parse(orchaProgram)
		
	}
	

}
