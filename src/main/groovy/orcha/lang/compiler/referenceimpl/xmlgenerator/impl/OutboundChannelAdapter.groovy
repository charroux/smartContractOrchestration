package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import com.sun.codemodel.JCodeModel
import com.sun.codemodel.JExpr
import com.sun.codemodel.JDefinedClass
import com.sun.codemodel.JDocComment
import com.sun.codemodel.JExpression
import com.sun.codemodel.JFieldVar
import com.sun.codemodel.JMethod
import com.sun.codemodel.JMod
import com.sun.codemodel.JVar
import com.sun.codemodel.writer.FileCodeWriter
import com.sun.codemodel.ClassType
import com.sun.codemodel.JBlock
import com.sun.codemodel.JClass
import groovy.util.logging.Slf4j
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.EventSourcing.JoinPoint
import orcha.lang.configuration.InputFileAdapter

import org.springframework.cloud.stream.messaging.Source

/**
 * 
 * An ASM version of Java classes can be generated using (ASM 5.2) :
 * java -cp asm-all-5.2.jar org.objectweb.asm.util.ASMifier *.class > *.java 
 * where * is a Java class.
 * 
 * @author benoit.charroux
 *
 */
@Slf4j
class OutboundChannelAdapter implements Chain, Transformer{
	
	File sourceCodeDirectory
	File binaryCodeDirectory
	OrchaCodeVisitor orchaCodeParser
	Document xmlSpringIntegration
	
	public OutboundChannelAdapter(File sourceCodeDirectory, File binaryCodeDirectory, OrchaCodeVisitor orchaCodeParser, Document xmlSpringIntegration) {
		super()
		this.sourceCodeDirectory = sourceCodeDirectory
		this.binaryCodeDirectory = binaryCodeDirectory
		this.orchaCodeParser = orchaCodeParser
		this.xmlSpringIntegration = xmlSpringIntegration;
	}

	public void file(InstructionNode instructionNode) {
		
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		def instruction = instructionNode.instruction
		
		def outputName = instruction.variable
		EventHandler eventHandler = instruction.springBean
		def outputChannel = outputName + "OutputFileChannelAdapter" + eventHandler.name
		String directoryExpression = "@" + eventHandler.name + ".output.adapter.directory"
		String filenameExpression = "@" + eventHandler.name + ".output.adapter.filename"
					
			if(instruction.variableProperty=="result" || instruction.variableProperty=="error"){
				
				Element chainElement = this.transform(instructionNode, instructionNode.inputName, outputChannel)
				rootElement.addContent(chainElement)
											
			} else if(instruction.variableProperty == "value"){		// case where the previous instruction is like: receive event from file
																	// and the current instruction is like: send event.value to output
			
				// look for the index i of the current instruction into all the inctructions
				int i=0
				while(i<instructions.size() && instructions.get(i)!=instruction){
					i++
				}
				
				// search the previous instruction (decrease i) so its variable matches
				i--
				while(i>=0 && instruction.variable!=instructions.get(i).variable){
					i--
				}
				
				def instructionToConnectToTheInput = instructions.get(i)
				
				Element channelElement = new Element("channel", namespace)
				channelElement.setAttribute("id", instructionToConnectToTheInput.springIntegrationOutputChannel)
				rootElement.addContent(channelElement)
				
				Element chainElement = this.transform(instructionNode, instructionToConnectToTheInput.springIntegrationOutputChannel, outputChannel)
				rootElement.addContent(chainElement)			
			
			} else {
				throw new OrchaCompilationException("Property for variable " + instruction.variable + " should be value or result")
			}
			
			
			Element channelElement = new Element("channel", namespace)
			channelElement.setAttribute("id", outputChannel)
			rootElement.addContent(channelElement)
			
			Element outboundAdapterElement = new Element("outbound-channel-adapter", Namespace.getNamespace("int-file", "http://www.springframework.org/schema/integration/file"))
			outboundAdapterElement.setAttribute("id", "file-"+outputName+eventHandler.name+"Channel-id")
			outboundAdapterElement.setAttribute("channel", outputChannel)
			outboundAdapterElement.setAttribute("directory-expression", directoryExpression)
			outboundAdapterElement.setAttribute("filename-generator-expression", filenameExpression)
			outboundAdapterElement.setAttribute("append-new-line", eventHandler.output.adapter.appendNewLine.toString())
			outboundAdapterElement.setAttribute("mode", eventHandler.output.adapter.writingMode.toString())
			outboundAdapterElement.setAttribute("auto-create-directory", eventHandler.output.adapter.createDirectory.toString())
			outboundAdapterElement.setAttribute("delete-source-files", "false")
			rootElement.addContent(outboundAdapterElement)					
		
	}
	

	public void file(InstructionNode instructionNode, EventHandler eventHandler1) {
		
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		def instruction = instructionNode.instruction
		
		//def outputName = instruction.variable
		Application eventHandler = instruction.springBean
		//def outputChannel = outputName + "OutputFileChannelAdapter" + eventHandler.name
		def outputChannel = instructionNode.inputName
		String directoryExpression = "@" + eventHandler1.name + ".input.adapter.directory"
		String filenameExpression = "@" + eventHandler1.name + ".input.adapter.filenamePattern"
					
		
/*			if(instruction.variableProperty=="result" || instruction.variableProperty=="error"){
				
				Element chainElement = this.transform(instructionNode, instructionNode.inputName, outputChannel)
				rootElement.addContent(chainElement)
											
			} else if(instruction.variableProperty == "value"){		// case where the previous instruction is like: receive event from file
																	// and the current instruction is like: send event.value to output
			
				// look for the index i of the current instruction into all the instructions
				int i=0
				while(i<instructions.size() && instructions.get(i)!=instruction){
					i++
				}
				
				// search the previous instruction (decrease i) so its variable matches
				i--
				while(i>=0 && instruction.variable!=instructions.get(i).variable){
					i--
				}
				
				def instructionToConnectToTheInput = instructions.get(i)
				
				Element channelElement = new Element("channel", namespace)
				channelElement.setAttribute("id", instructionToConnectToTheInput.springIntegrationOutputChannel)
				rootElement.addContent(channelElement)
				
				Element chainElement = this.transform(instructionNode, instructionToConnectToTheInput.springIntegrationOutputChannel, outputChannel)
				rootElement.addContent(chainElement)			
			
			} else {
				throw new OrchaCompilationException("Property for variable " + instruction.variable + " should be value or result")
			}*/
			
			
			Element channelElement = new Element("channel", namespace)
			channelElement.setAttribute("id", outputChannel)
			rootElement.addContent(channelElement)
			
			Element outboundAdapterElement = new Element("outbound-channel-adapter", Namespace.getNamespace("int-file", "http://www.springframework.org/schema/integration/file"))
			outboundAdapterElement.setAttribute("id", "file-"+outputChannel+"Channel-id")
			outboundAdapterElement.setAttribute("channel", outputChannel)
			outboundAdapterElement.setAttribute("directory-expression", directoryExpression)
			outboundAdapterElement.setAttribute("filename-generator-expression", filenameExpression)
			/*outboundAdapterElement.setAttribute("append-new-line", eventHandler.output.adapter.appendNewLine.toString())
			outboundAdapterElement.setAttribute("mode", eventHandler.output.adapter.writingMode.toString())
			outboundAdapterElement.setAttribute("auto-create-directory", eventHandler.output.adapter.createDirectory.toString())*/
			outboundAdapterElement.setAttribute("delete-source-files", "false")
			rootElement.addContent(outboundAdapterElement)					
		
	}
	
	private Element transform(InstructionNode instructionNode, String inputChannel, String outputChannel){
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")		
		
		def instruction = instructionNode.instruction
		
		EventHandler eventHandler = instruction.springBean
		
		Element chainElement = chain(inputChannel, outputChannel)
		
		if(instructionNode.options!=null && instructionNode.options.eventSourcing!=null){
			if(instructionNode.options.eventSourcing.joinPoint==JoinPoint.before){
					
				Element transformer = new Element("transformer", namespace)
				transformer.setAttribute("expression", "payload")
				chainElement.addContent(transformer)
				
				Element adviceChain = new Element("request-handler-advice-chain", namespace)
				transformer.addContent(adviceChain)
				
				Element eventSourcingElement = eventSourcing(instructionNode.options.eventSourcing)
				adviceChain.addContent(eventSourcingElement)
					
				/*Element adviceChain = new Element("request-handler-advice-chain", namespace)
				transformer.addContent(adviceChain)
					
				Element refElement = new Element("ref", Namespace.getNamespace("", "http://www.springframework.org/schema/beans"))
				refElement.setAttribute("bean", "eventSourcingAdvice")
				adviceChain.addContent(refElement)*/
				
			}
		}
			
		if(eventHandler.output.mimeType == "text/plain"){
			Element objectToStringElement = objectToString()
			chainElement.addContent(objectToStringElement)
		} else if(eventHandler.output.mimeType == "application/json"){
			Element objectToJsonElement = objectToJson()
			chainElement.addContent(objectToJsonElement)
		}
		
		return chainElement
	}
	
	private boolean isAMessagingMiddleWarePartitionAlreadyDone(InstructionNode instructionNode) {
		def IDs = orchaCodeParser.computeInstructionIDsFromTheSameEventAs(instructionNode)
		def orchaIDs = IDs.findAll{ orchaCodeParser.isAnOrchaApplication(it) == true }
		int index = orchaIDs.indexOf(instructionNode.instruction.id)
		if(index > 0) {
			return true
		} else {
			return false
		}
	}
	
	public void messagingMiddleware(InstructionNode instructionNode, String partitionKeyExpression) {
				
		if(this.isAMessagingMiddleWarePartitionAlreadyDone(instructionNode) == true) {
			return
		}
		
		Instruction instruction = instructionNode.instruction
				
		def inputChannel = instructionNode.inputName
				
		Element rootElement = xmlSpringIntegration.getRootElement()

		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element element = new Element("channel", namespace)
		element.setAttribute("id", inputChannel)
		rootElement.addContent(element)
		
		if(instruction.springBean instanceof Application && instruction.springBean.language.equalsIgnoreCase("Orcha")) {
			String mimeType = instruction.springBean.input.adapter.outputEventHandler.output.mimeType
			if(mimeType!=null && mimeType.split("/").length != 2){
				throw new OrchaCompilationException("Unknown Mime Type:" + mimeType)
			}
		} else {
			String mimeType = instruction.springBean.output.mimeType
			if(mimeType!=null && mimeType.split("/").length != 2){
				throw new OrchaCompilationException("Unknown Mime Type:" + instruction.springBean.output.mimeType)
			}
		}
						
		String streamHandlerClassName = "orcha.lang.generated.OutputStreamHandler"
		//String s = "." + File.separator + "src" + File.separator + "main" + File.separator + "groovy"
		String s = sourceCodeDirectory.getAbsolutePath() + File.separator + "groovy"
		String path = s + File.separator + streamHandlerClassName + ".groovy"
		
		log.info 'Generation of the stream handler for sending an event to a messaging middleware: ' + path
		
		JCodeModel codeModel = new JCodeModel();
		
		JDefinedClass streamHandlerClass = codeModel._class(JMod.PUBLIC, streamHandlerClassName, ClassType.CLASS)
		streamHandlerClass.annotate(org.springframework.cloud.stream.annotation.EnableBinding.class).param("value", Source.class)
		
		JDocComment jDocComment = streamHandlerClass.javadoc();
		jDocComment.add(String.format("Do not edit this file : auto generated file"));
		
		JFieldVar loggerField = streamHandlerClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, org.slf4j.Logger.class, "log");
		JClass loggerFactoryClass = codeModel.ref("org.slf4j.LoggerFactory");
		loggerField.init(loggerFactoryClass.staticInvoke("getLogger").arg(streamHandlerClass.dotclass()))
		
		JMethod method = streamHandlerClass.method(JMod.PUBLIC, Object.class, "transform")
		Class paramClass = Class.forName("java.lang.Object")
		JVar nameParam = method.param(paramClass, "message");
		
		method.annotate(codeModel.ref(org.springframework.integration.annotation.Transformer.class)).param("inputChannel", inputChannel).param("outputChannel", Source.OUTPUT)
		
		JBlock body = method.body()
		
		JExpression argu = JExpr.lit("Sending message to the messaging middleware: ")
		argu = argu.plus(nameParam)
		body.add(JExpr.invoke(loggerField, "info").arg(argu))
		
		body._return(nameParam)
		
		FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(s))
		codeModel.build(fileCodeWriter)
		fileCodeWriter.close()

		log.info 'Generation of the stream handler for sending an event to a messaging middleware: ' + path + ' complete successfully.'

		
		String tt = streamHandlerClassName.replaceAll('\\.', "/")
		
		String[] packagePathElements = tt.split("/");
		//String fichier = "." + File.separator + "bin" + File.separator + "main" + File.separator
		String fichier = binaryCodeDirectory.getAbsolutePath()
		
		for(int i=0; i<packagePathElements.length-1; i++) {
			fichier = fichier + File.separator + packagePathElements[i]
		}
				
		fichier = fichier + File.separator + packagePathElements[packagePathElements.length-1] + ".class";

		log.info 'Generation of the stream handler binary class for sending an event to a messaging middleware: ' + fichier
		
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;
		
		cw.visit(52, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "orcha/lang/generated/OutputStreamHandler", null, "java/lang/Object", null);
		
		av0 = cw.visitAnnotation("Lorg/springframework/cloud/stream/annotation/EnableBinding;", true);
		
		AnnotationVisitor av1 = av0.visitArray("value");
		av1.visit(null, Type.getType("Lorg/springframework/cloud/stream/messaging/Source;"));
		av1.visitEnd();
		
		av0.visitEnd();
		
		fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, "log", "Lorg/slf4j/Logger;", null, null);
		fv.visitEnd();
		
		mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
		mv.visitCode();
		mv.visitLdcInsn(Type.getType("Lorcha/lang/generated/OutputStreamHandler;"));
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/slf4j/LoggerFactory", "getLogger", "(Ljava/lang/Class;)Lorg/slf4j/Logger;", false);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, "orcha/lang/generated/OutputStreamHandler", "log", "Lorg/slf4j/Logger;");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 0);
		mv.visitEnd();
		
		
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "transform", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
		
		av0 = mv.visitAnnotation("Lorg/springframework/integration/annotation/Transformer;", true);
		av0.visit("inputChannel", inputChannel);
		
		av0.visit("outputChannel", "output");
		av0.visitEnd();
		
		/*mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(1, 2);
		mv.visitEnd();*/
		
		mv.visitCode();
		mv.visitFieldInsn(Opcodes.GETSTATIC, "orcha/lang/generated/OutputStreamHandler", "log", "Lorg/slf4j/Logger;");
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn("Sending message to the messaging middleware: ");
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;)V", true);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(4, 2);
		mv.visitEnd();
				
		cw.visitEnd();
		
		byte[] bytes = cw.toByteArray()
		
		File file = new File(fichier);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();
		
		log.info 'Generation of the stream handler binary class for sending an event to a messaging middleware: ' + fichier + ' complete successfully'
		
		//fichier = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "application.properties"
		fichier = sourceCodeDirectory.absolutePath + File.separator + "resources" + File.separator + "application.properties"

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
spring.cloud.stream.bindings.output.destination=''' + destinationName
		
		if(orchaCodeParser.isAMessagingPartition(instructionNode)) {
			
			src << '''

# Auto generation of the partitionKeyExpression for the messaging middleware. Do not delete this line:
spring.cloud.stream.bindings.output.producer.partitionKeyExpression=''' + partitionKeyExpression << '''
# Auto generation of the output partitionCount for the messaging middleware. The partition index value is calculated as hashCode(key) % partitionCount. Do not delete this line:
spring.cloud.stream.bindings.output.producer.partitionCount=2'''

		}
		
		log.info 'Adding the output binding destination ' + destinationName + ' to: ' + fichier + ' complete successfully'
		
		//fichier = "." + File.separator + "bin" + File.separator + "main" + File.separator + "application.properties"
		fichier = binaryCodeDirectory.absolutePath + File.separator + "application.properties"
		
		log.info 'Adding the output binding destination ' + destinationName + ' to: ' + fichier
		
		File dst = new File(fichier)
		
		dst << src.text

		log.info 'Adding the output binding destination ' + destinationName + ' to: ' + fichier + ' complete successfully'
		
	}

}
