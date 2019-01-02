package orcha.lang.compiler.referenceimpl.configurationproperties

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.MessagingMiddlewareAdapter
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
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j

import java.io.File
import java.util.concurrent.atomic.DoubleAdder

import javax.validation.constraints.NotNull

import org.springframework.boot.context.properties.ConfigurationProperties

@Slf4j
class ConfigurationPropertiesGenerator {
	
	void resetSpringCloudStream(File sourceCodeDirectory, File binaryCodeDirectory) {
		
		String fichier = sourceCodeDirectory.absolutePath + File.separator + "resources" + File.separator + "application.properties"
		
		
		def lines = []
		
		File src = new File(fichier)
		
		if(src.exists()) {
			
			log.info 'Reset Spring Cloud Stream property file begins ' + fichier

			src.eachLine {
				line -> if(line.startsWith("spring.cloud.stream.bindings.output")==false && line.startsWith("spring.cloud.stream.bindings.input")==false && line.startsWith("spring.cloud.stream.instance")==false && line.startsWith("# Auto generation of the input")==false && line.startsWith("# Auto generation of the output")==false) lines.add(line)
			}
			
			src.delete()
			
			src = new File(fichier)
			
			src.withWriter('utf-8') { writer ->
				lines.each{
					writer.writeLine(it)
				}
			}
			
			log.info 'Reset Spring Cloud Stram property terminates successfuly ' + fichier
	
			fichier = binaryCodeDirectory.absolutePath + File.separator + "application.properties"
			
			log.info 'Reset Spring Cloud Stram property file begins ' + fichier
			
			File dst = new File(fichier)
			
			dst << src.text
	
			log.info 'Reset Spring Cloud Stram property terminates successfuly ' + fichier
	
		}
		
	}
	
	void configureSpringCloudInputStream(InstructionNode instructionNode, File sourceCodeDirectory, File binaryCodeDirectory) {
	
		Instruction instruction = instructionNode.instruction
		
		String fichier = sourceCodeDirectory.absolutePath + File.separator + "resources" + File.separator + "application.properties"
		
		String destinationName
		
		if(instruction.springBean instanceof Application) {
			destinationName = instruction.springBean.output.adapter.inputEventHandler.name
		} else {
			destinationName = instruction.springBean.name
		}
		
		def lines = []
		
		File src = new File(fichier)
		
		src.eachLine {
			line -> if(line.startsWith("spring.cloud.stream.bindings.input")==false && line.startsWith("spring.cloud.stream.instance")==false && line.startsWith("# Auto generation of the input")==false && line.equals("")==false) lines.add(line)
		}
		
		src.delete()
		
		src = new File(fichier)
		
		src.withWriter('utf-8') { writer ->
			lines.each{
				writer.writeLine(it)
			}
		}
		
		log.info 'Adding the input binding destination ' + destinationName + ' to: ' + fichier

		src << '''

# Auto generation of the input destination to the messaging middleware. Do not delete this line:'''

		src << '''
spring.cloud.stream.bindings.input.content-type=application/json
spring.cloud.stream.bindings.input.destination=''' + destinationName
		
		if(instruction.springBean instanceof EventHandler && instruction.springBean.input!=null) {
			MessagingMiddlewareAdapter messagingMiddlewareAdapter = instruction.springBean.input.adapter
			if(messagingMiddlewareAdapter.partitioned == true) {
		src << '''
# Auto generation of the configuration for the partitioning. Do not delete this line:
spring.cloud.stream.bindings.input.consumer.partitioned=true
spring.cloud.stream.instanceIndex=''' + messagingMiddlewareAdapter.partitionNumber
		src << '''
spring.cloud.stream.instanceCount=''' + messagingMiddlewareAdapter.instanceCount
				if(messagingMiddlewareAdapter.groupName != null) {
					src << '''
spring.cloud.stream.bindings.input.group=''' + messagingMiddlewareAdapter.groupName				
				} else {
		src << '''
spring.cloud.stream.bindings.input.group=''' + instruction.springBean.name + "Group"
				}
			}
		}
		
		log.info 'Adding the input binding destination ' + destinationName + ' to: ' + fichier + ' complete successfully'

		fichier = binaryCodeDirectory.absolutePath + File.separator + "application.properties"
		
		log.info 'Adding the input binding destination ' + destinationName + ' to: ' + fichier
		
		File dst = new File(fichier)
		
		dst << src.text

		log.info 'Adding the input binding destination ' + destinationName + ' to: ' + fichier + ' complete successfully'

	}
	
	void configureSpringCloudOutputStream(OrchaCodeVisitor orchaCodeParser, InstructionNode instructionNode, File sourceCodeDirectory, File binaryCodeDirectory, String partitionKeyExpression) {

		Instruction instruction = instructionNode.instruction
		
		String fichier = sourceCodeDirectory.absolutePath + File.separator + "resources" + File.separator + "application.properties"

		String destinationName
		
		if(instruction.springBean instanceof Application) {
			destinationName = instruction.springBean.input.adapter.outputEventHandler.name
		} else {
			destinationName = instruction.springBean.name
		}
		

		log.info 'Adding the output binding destination ' + destinationName + ' to: ' + fichier
		
		def lines = []
		
		File src = new File(fichier)
		
		src.eachLine {
			line -> if(line.startsWith("spring.cloud.stream.bindings.output")==false && line.startsWith("# Auto generation of the")==false && line.equals("")==false) lines.add(line)
		}
		
		src.delete()
		
		src = new File(fichier)
		
		src.withWriter('utf-8') { writer ->
			lines.each{
				writer.writeLine(it)
			}
		}
		
		src << '''

# Auto generation of the output destination to the messaging middleware. Do not delete this line:
spring.cloud.stream.bindings.output.content-type=application/json
spring.cloud.stream.bindings.output.destination=''' + destinationName
		
		if(orchaCodeParser.isAMessagingPartition(instructionNode)) {
			
			src << '''

# Auto generation of the output partitionKeyExpression for the messaging middleware. Do not delete this line:
spring.cloud.stream.bindings.output.producer.partitionKeyExpression=''' + partitionKeyExpression << '''
# Auto generation of the output partitionCount for the messaging middleware. The partition index value is calculated as hashCode(key) % partitionCount. Do not delete this line:
spring.cloud.stream.bindings.output.producer.partitionCount=2'''

		}
		
		log.info 'Adding the output binding destination ' + destinationName + ' to: ' + fichier + ' complete successfully'
		
		fichier = binaryCodeDirectory.absolutePath + File.separator + "application.properties"
		
		log.info 'Adding the output binding destination ' + destinationName + ' to: ' + fichier
		
		File dst = new File(fichier)
		
		dst << src.text

		log.info 'Adding the output binding destination ' + destinationName + ' to: ' + fichier + ' complete successfully'
		
	}

	
	void generate(OrchaCodeVisitor orchaCodeParser){
		
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
