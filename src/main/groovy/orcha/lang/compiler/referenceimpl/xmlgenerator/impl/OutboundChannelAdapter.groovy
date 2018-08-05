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
import com.sun.codemodel.JDefinedClass
import com.sun.codemodel.JDocComment
import com.sun.codemodel.JMethod
import com.sun.codemodel.JMod
import com.sun.codemodel.JVar
import com.sun.codemodel.writer.FileCodeWriter
import com.sun.codemodel.ClassType
import com.sun.codemodel.JBlock
import groovy.util.logging.Slf4j
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaCompilationException
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
	
	Document xmlSpringIntegration
	
	public OutboundChannelAdapter(Document xmlSpringIntegration) {
		super();
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
	
	public void messagingMiddleware(InstructionNode instructionNode) {
				
		Instruction instruction = instructionNode.instruction
		def inputChannel = instructionNode.inputName
				
		Element rootElement = xmlSpringIntegration.getRootElement()

		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element element = new Element("channel", namespace)
		element.setAttribute("id", inputChannel)
		rootElement.addContent(element)
		
		String[] typeAndSubtype = instruction.springBean.output.mimeType.split("/");
		if(typeAndSubtype.length != 2){
			throw new OrchaCompilationException("Unknown Mime Type:" + instruction.springBean.output.mimeType)
		}
					
		String streamHandlerClassName = "orcha.lang.generated.OutputStreamHandler"
		String s = "." + File.separator + "src" + File.separator + "main" + File.separator + "java"
		String path = s + File.separator + streamHandlerClassName + ".java"
		
		log.info 'Generation of the stream handler for sending an event to a messaging middleware: ' + path
		
		JCodeModel codeModel = new JCodeModel();
		
		JDefinedClass streamHandlerClass = codeModel._class(JMod.PUBLIC, streamHandlerClassName, ClassType.CLASS)
		streamHandlerClass.annotate(org.springframework.cloud.stream.annotation.EnableBinding.class).param("value", Source.class)
		
		JDocComment jDocComment = streamHandlerClass.javadoc();
		jDocComment.add(String.format("Do not edit this file : auto generated file"));
		
		JMethod method = streamHandlerClass.method(JMod.PUBLIC, Object.class, "transform")
		Class paramClass = Class.forName("java.lang.Object")
		JVar nameParam = method.param(paramClass, "message");
		
		method.annotate(codeModel.ref(org.springframework.integration.annotation.Transformer.class)).param("inputChannel", inputChannel).param("outputChannel", Source.OUTPUT)
		
		JBlock body = method.body()
		
		body._return(nameParam)
		
		FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(s))
		codeModel.build(fileCodeWriter)
		fileCodeWriter.close()

		log.info 'Generation of the stream handler for sending an event to a messaging middleware: ' + path + ' complete successfully.'

		
		String tt = streamHandlerClassName.replaceAll('\\.', "/")
		
		String[] packagePathElements = tt.split("/");
		String fichier = "." + File.separator + "bin" + File.separator + "main" + File.separator
		
		for(int i=0; i<packagePathElements.length-1; i++) {
			fichier = fichier + packagePathElements[i] + File.separator
		}
				
		fichier = fichier + packagePathElements[packagePathElements.length-1] + ".class";

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
		
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(1, 2);
		mv.visitEnd();
		
		cw.visitEnd();
		
		byte[] bytes = cw.toByteArray()
		
		File file = new File(fichier);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();
		
		log.info 'Generation of the stream handler binary class for sending an event to a messaging middleware: ' + fichier + ' complete successfully'
		
		/*
		log.info 'Generation of the stream handler binary file for receiving event from a messaging middleware: ' + fichier
		
		cw = new ClassWriter(0);
		
		cw.visit(52, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, tt, null, "java/lang/Object", null);
		
		av0 = cw.visitAnnotation("Lorg/springframework/cloud/stream/annotation/EnableBinding;", true);
		
		AnnotationVisitor av1 = av0.visitArray("value");
		av1.visit(null, Type.getType("Lorg/springframework/cloud/stream/messaging/Sink;"));
		av1.visitEnd();
		
		av0.visitEnd();
		
		fv = cw.visitField(Opcodes.ACC_PRIVATE, "context", "Lorg/springframework/context/ApplicationContext;", null, null);
		
		av0 = fv.visitAnnotation("Lorg/springframework/beans/factory/annotation/Autowired;", true);
		av0.visitEnd();
		
		fv.visitEnd();
		
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
			
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "handle", "(Ljava/lang/Integer;)V", null, null);
		
		av0 = mv.visitAnnotation("Lorg/springframework/cloud/stream/annotation/StreamListener;", true);
		av0.visit("value", "input");
		av0.visitEnd();
		
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, tt, "context", "Lorg/springframework/context/ApplicationContext;");
		mv.visitLdcInsn(Type.getType("L" + t + ";"));
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/springframework/context/ApplicationContext", "getBean", "(Ljava/lang/Class;)Ljava/lang/Object;", true);
		mv.visitTypeInsn(Opcodes.CHECKCAST, t);
		mv.visitVarInsn(Opcodes.ASTORE, 2);
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, t, "call", "(Ljava/lang/Integer;)V", true);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
		
		bytes = cw.toByteArray()
						
		file = new File(fichier);
		fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();

		log.info 'Generation of the stream handler binary file for receiving event from a messaging middleware: ' + fichier + ' complete successfully.'
		*/
	}

}
