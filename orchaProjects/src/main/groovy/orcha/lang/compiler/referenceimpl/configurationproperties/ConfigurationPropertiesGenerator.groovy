package orcha.lang.compiler.referenceimpl.configurationproperties

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.configuration.Output
import com.sun.codemodel.JCodeModel
import com.sun.codemodel.JDefinedClass
import com.sun.codemodel.JDocComment
import com.sun.codemodel.JFieldVar
import com.sun.codemodel.JMethod
import com.sun.codemodel.JMod
import com.sun.codemodel.ClassType
import com.sun.codemodel.JBlock
import com.sun.codemodel.JClass
import com.sun.codemodel.JExpr
import com.sun.codemodel.writer.FileCodeWriter
import com.sun.org.apache.xpath.internal.functions.FuncSubstring
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j

import java.util.concurrent.atomic.DoubleAdder

import javax.validation.constraints.NotNull

import org.springframework.boot.context.properties.ConfigurationProperties

@Slf4j
class ConfigurationPropertiesGenerator {
	
	void generate(OrchaCodeParser orchaCodeParser){
		
		log.info 'Configuration property generation begins'
		
		def configurationMetadata = [:]
		def groupsMetadata = []		
		def propertiesMetadata = []
		
		JCodeModel codeModel = new JCodeModel();
		def className = 'orcha.lang.configure.ConfigurationProperties'
		JDefinedClass contractPropertyClass = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS)
		
		JDocComment jDocComment = contractPropertyClass.javadoc();
		jDocComment.add(String.format("Do not edit this file : auto generated file"));
		
		String domain = "driving"
		contractPropertyClass.annotate(ConfigurationProperties.class).param("prefix", domain)
		
		def metadata = [:]
		metadata.put("sourceType", className)
		metadata.put("name", domain)
		metadata.put("type", className)
		groupsMetadata.add(metadata)
		
		List<InstructionNode> nodes = orchaCodeParser.findAllNodes()
		
		nodes.each {
			
			Instruction instruction = it.instruction;
			
			if(instruction.springBean != null){
				
				Output output = instruction.springBean.output
				
				if(output!=null && output.adapter!=null){
					
					def properties = output.adapter.properties
					
					if(properties.size() > 0){

						String embedddedClassName = instruction.springBean.name
						embedddedClassName = embedddedClassName.substring(0,1).toUpperCase().concat(embedddedClassName.substring(1))
						JDefinedClass embeddedClass = contractPropertyClass._class(JMod.PUBLIC, embedddedClassName)
						
						properties.each { property ->
							
							log.info 'Configuration property generation: ' + property + ' can be set for ' + embedddedClassName + ' before launching the Orcha program into META-INF/application.properties'
								
							JFieldVar field = embeddedClass.field(JMod.PUBLIC, String.class, property)
							
							String methodName = property.substring(0,1).toUpperCase().concat(property.substring(1))
							JMethod method = embeddedClass.method(JMod.PUBLIC, String.class, "get" + methodName)
							JBlock body = method.body();
							body._return(field)
							
							method = embeddedClass.method(JMod.PUBLIC, void.class, "set" + methodName)
							method.param(String.class, property);
							body = method.body();
							body.assign(JExpr._this().ref(field.name()), JExpr.ref(field.name()));
							
							metadata = [:]
							metadata.put("sourceType",className + '$' + embedddedClassName)
							metadata.put("name", domain + "." + instruction.springBean.name + "." + property)
							metadata.put("type", "java.lang.String")
							propertiesMetadata.add(metadata)
						}
						
						JFieldVar field = contractPropertyClass.field(JMod.PUBLIC, embeddedClass, instruction.springBean.name)
						JClass serviceReference = codeModel.ref(embedddedClassName);
						field.init(JExpr._new(serviceReference));
						
						String methodName = instruction.springBean.name
						methodName = methodName.substring(0,1).toUpperCase().concat(methodName.substring(1))
						JMethod method = contractPropertyClass.method(JMod.PUBLIC, embeddedClass, "get" + methodName)
						JBlock body = method.body();
						body._return(field)
						
						method = contractPropertyClass.method(JMod.PUBLIC, void.class, "set" + methodName)
						method.param(embeddedClass, instruction.springBean.name);
						body = method.body();
						body.assign(JExpr._this().ref(field.name()), JExpr.ref(field.name()));
	
						metadata = [:]
						metadata.put("sourceType", className)
						metadata.put("name", domain + "." + instruction.springBean.name)
						metadata.put("sourceMethod", "get" + methodName)
						metadata.put("type", className + '$' + embedddedClassName)
						groupsMetadata.add(metadata)
					}
					
				}
	
			}
		}
		
					
		String s = "." + File.separator + "src" + File.separator + "main" + File.separator + "java"
		FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(s))
		codeModel.build(fileCodeWriter)
		fileCodeWriter.close()
		
		configurationMetadata.put("hints", [])
		configurationMetadata.put("groups", groupsMetadata)
		configurationMetadata.put("properties", propertiesMetadata)
	
		String json = JsonOutput.toJson(configurationMetadata)
		
		String springConfigurationMetadata = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "META-INF" + File.separator + "spring-configuration-metadata.json"
		
		new File(springConfigurationMetadata).withWriter('utf-8') { writer ->
			writer.writeLine json
		}
		
		log.info 'Configuration property generated successfully. Property metadata file is: ' + springConfigurationMetadata
		
	}

}