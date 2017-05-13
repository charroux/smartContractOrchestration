package orcha.lang.compiler.serviceOffer

import java.util.List
import java.util.Map

import groovy.util.logging.Slf4j
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.configuration.Application

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.context.annotation.Configuration

import java.lang.reflect.Method


@Slf4j
class ServiceOfferSelectionGenerator {
	
	private Map<Class, List<InstructionNode>> getBeansByConfigurationClass(OrchaCodeParser orchaCodeParser){
		
		def beansByConfigurationClass = [:]
		
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
		
		List<InstructionNode> computeNodes = orchaCodeParser.findAllComputeNodes()
		
		for (BeanDefinition beanDef : provider.findCandidateComponents("configuration.*")) {
			
			Class<?> configurationClass = Class.forName(beanDef.getBeanClassName());
			Configuration findable = configurationClass.getAnnotation(Configuration.class);
			
			Method[] methods = configurationClass.getMethods();
			
			def beans = []
			
			for(Method method: methods){
				InstructionNode instruction = computeNodes.find{ it.instruction.springBean.input!=null && it.instruction.springBean.input.adapter==null && it.instruction.springBean.output!=null && it.instruction.springBean.output.adapter==null && it.instruction.springBean.name.equals(method.getName())}
				if( instruction != null){
					beans.add(instruction)
				}
			}
			
			if(beans.empty == false){
				beansByConfigurationClass[configurationClass] = beans
			}
			
		}
		
		return beansByConfigurationClass
	}
	
	boolean generate(OrchaCodeParser orchaCodeParser){
		
		def beansByConfigurationClass = this.getBeansByConfigurationClass(orchaCodeParser)
		
		if(beansByConfigurationClass.size() == 0){
			return false
		}
		
		
		beansByConfigurationClass.each{ entry, value ->
			
			Class configurationClass = entry
			
			def instructionNodes = value
						
			instructionNodes.each{ instructionNode ->
				
				Instruction instruction = instructionNode.instruction
										
				if(instruction.springBean instanceof Application){
					
					Application application = (Application)instruction.springBean
					application.input.type
					application.output.type
					
					//def http = new HTTPBuilder(uri)
				}
			}
		}
		
		return true
		
	}

}
