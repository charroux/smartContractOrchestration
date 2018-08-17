package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import com.sun.codemodel.JCodeModel
import com.sun.codemodel.JDefinedClass
import com.sun.codemodel.JDocComment
import com.sun.codemodel.JFieldVar
import com.sun.codemodel.JMethod
import com.sun.codemodel.JMod
import com.sun.codemodel.JVar
import com.sun.codemodel.JExpr
import com.sun.codemodel.JExpression
import com.sun.codemodel.JInvocation
import com.sun.codemodel.writer.FileCodeWriter

import groovy.util.logging.Slf4j

import com.sun.codemodel.ClassType
import com.sun.codemodel.JAnnotationUse
import com.sun.codemodel.JBlock
import com.sun.codemodel.JClass
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.qualityOfService.QueueOption
import orcha.lang.configuration.Application
import orcha.lang.configuration.HttpAdapter
import orcha.lang.configuration.Input
import org.springframework.http.MediaType

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.messaging.Sink

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
class InboundChannelAdapter implements Poller, Chain, HeaderEnricher, Filter, Transformer {
	
	Document xmlSpringIntegration
	String filteringExpression
	
	public InboundChannelAdapter(Document xmlSpringIntegration, String filteringExpression) {
		super();
		this.xmlSpringIntegration = xmlSpringIntegration
		this.filteringExpression = filteringExpression
		
		println filteringExpression
	}
	
	private Element addDefaultPoller(InstructionNode instructionNode) {

		Element pollerElement

			QueueOption queueOption = new QueueOption(fixedDelay: 1000)
			pollerElement = poller(queueOption)
			pollerElement.setAttribute("default", "true")
	
		return pollerElement
	}
	
	public void file(InstructionNode instructionNode) {

		Element rootElement = xmlSpringIntegration.getRootElement()	

		Element pollerElement = this.addDefaultPoller(instructionNode)
		rootElement.addContent(pollerElement)

		Instruction instruction = instructionNode.instruction
		def inputChannel = instructionNode.inputName
		
		Namespace namespace = Namespace.getNamespace("int-file", "http://www.springframework.org/schema/integration/file")
		
		Element fileAdapter = new Element("inbound-channel-adapter", namespace)
		fileAdapter.setAttribute("id", "file-"+inputChannel+"-id")
		fileAdapter.setAttribute("directory", instruction.springBean.input.adapter.directory)
		fileAdapter.setAttribute("channel", inputChannel)
		fileAdapter.setAttribute("prevent-duplicates", "true")
		
		if(instruction.springBean.input.adapter.filenamePattern != null){
			fileAdapter.setAttribute("filename-pattern", instruction.springBean.input.adapter.filenamePattern)
		}
		
		if(instructionNode.options!=null && instructionNode.options.queue!=null){
			fileAdapter.setAttribute("queue-size", instructionNode.options.queue.capacity.toString())
		}
		
		rootElement.addContent(fileAdapter)
		
		Element stringTransformer = new Element("file-to-string-transformer", namespace)
		stringTransformer.setAttribute("input-channel", inputChannel)
		stringTransformer.setAttribute("output-channel", inputChannel+"Transformer")
		stringTransformer.setAttribute("delete-files", "false")
		rootElement.addContent(stringTransformer)
			
		String[] typeAndSubtype = instruction.springBean.input.mimeType.split("/");

		if(typeAndSubtype.length != 2){
			throw new OrchaCompilationException("Unknown Mime Type:" + instruction.springBean.input.mimeType)
		}
			
		Element chain = chain(inputChannel+"Transformer", instructionNode.outputName)
		rootElement.addContent(chain)
			
		MediaType mediaType = new  MediaType(typeAndSubtype[0], typeAndSubtype[1])
			
		if(mediaType.equals(MediaType.APPLICATION_JSON)){								
			Element jsonToObjectTransformer = jsonToObject(instruction.springBean.input.type) 
			chain.addContent(jsonToObjectTransformer)				
		}
				
		def id = "headers['id'].toString()"
		Element messageIDEnricher = headerEnricher("messageID", id)
		chain.addContent(messageIDEnricher)
			
		if(filteringExpression != null) {
			Element conditionFilter = filter(filteringExpression)
			chain.addContent(conditionFilter)
		}
		
		/*if(instructionNode.next.instruction.instruction!="receive" && instructionNode.instruction.condition!=null){
			String condition = instructionNode.instruction.condition
			
			condition = condition.trim()
			
			if(condition.startsWith("==")) {
				condition = "payload " + condition
			} else {
				condition = "payload." + condition
			}
			
			
		}*/
				
	}

	public void messagingMiddleware(InstructionNode instructionNode, String title) {
		
		String gateWayClassName = "orcha.lang.generated." + title.replaceAll("\\s","") + "Gateway"
				
		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Instruction instruction = instructionNode.instruction
		def inputChannel = instructionNode.inputName
		
		if(instruction.springBean instanceof Application) {
			inputChannel =inputChannel + "Orcha"			
		}
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element gateway = new Element("gateway", namespace)
		gateway.setAttribute("id", "gateway-"+inputChannel+"-id")
		gateway.setAttribute("service-interface", gateWayClassName)
		gateway.setAttribute("default-request-channel", inputChannel)
		
		rootElement.addContent(gateway)
		
		if(instruction.springBean instanceof Application && instruction.springBean.language.equalsIgnoreCase("Orcha")) {
			String mimeType = instruction.springBean.output.adapter.inputEventHandler.input.mimeType
			if(mimeType!=null && mimeType.split("/").length != 2){
				throw new OrchaCompilationException("Unknown Mime Type:" + mimeType)
			}
		} else {
			String mimeType = instruction.springBean.input.mimeType
			if(mimeType!=null && mimeType.split("/").length != 2){
				throw new OrchaCompilationException("Unknown Mime Type:" + instruction.springBean.input.mimeType)
			}
		}
			
		Element c
		
		if(instruction.springBean instanceof Application) {
			c = chain(inputChannel, instruction.springBean.name + "ServiceAcivatorOutput")
		} else {
			c = chain(inputChannel, instructionNode.outputName)
		}
		
		rootElement.addContent(c)
								
		def id = "headers['id'].toString()"
		Element messageIDEnricher = headerEnricher("messageID", id)
		c.addContent(messageIDEnricher)
		
		if(filteringExpression != null) {
			Element conditionFilter = filter(filteringExpression)
			c.addContent(conditionFilter)
		}
		
		if(instruction.springBean instanceof Application) {
			Element objectToApplicationTransformerElement = objectToApplicationTransformer(instructionNode)
			rootElement.addContent(objectToApplicationTransformerElement)
		}
		
		// generate gateway interface source code
		
		String s = "." + File.separator + "src" + File.separator + "main" + File.separator + "java"
		String path = s + File.separator + gateWayClassName + ".java"
		
		log.info 'Generation of the gateway interface for receiving event from a messaging middleware: ' + path
		
		JCodeModel codeModel = new JCodeModel();
		
		JDefinedClass gatewayInterace = codeModel._class(JMod.PUBLIC, gateWayClassName, ClassType.INTERFACE)
		
		JDocComment jDocComment = gatewayInterace.javadoc();
		jDocComment.add(String.format("Do not edit this file : auto generated file"));
		
		JMethod method = gatewayInterace.method(JMod.PUBLIC, gatewayInterace.owner().VOID, "call");
		
		String paramType
		
		if(instruction.springBean instanceof Application) {
			paramType = instruction.springBean.output.adapter.inputEventHandler.input.type
		} else {
			paramType = instruction.springBean.input.type
		}
		
		Class paramClass = Class.forName(paramType)
		JVar nameParam = method.param(paramClass, "event");
		
	
		FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(s))
		codeModel.build(fileCodeWriter)
		fileCodeWriter.close()

		log.info 'Generation of the gateway interface for receiving event from a messaging middleware: ' + path + ' complete succefully.'
		
		// generate gateway interface binary code
		
		s = "." + File.separator + "bin" + File.separator + "main"
		path = s + File.separator + gateWayClassName + ".class"
		
		log.info 'Generation of the binary file gateway interface for receiving event from a messaging middleware: ' + path
		
		String t = gateWayClassName.replaceAll('\\.', "/")
		
		paramType = paramType.replaceAll('\\.', "/")
		paramType = "(L" + paramType + ";)V"
		
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;
		
		cw.visit(52, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE, t, null, "java/lang/Object", null);
		
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, "call", paramType, null, null);
		mv.visitEnd();
		
		cw.visitEnd();
		
		byte[] bytes = cw.toByteArray()
		
		String[] packagePathElements = t.split("/");
		String fichier = "." + File.separator + "bin" + File.separator + "main" + File.separator
		
		for(int i=0; i<packagePathElements.length-1; i++) {
			fichier = fichier + packagePathElements[i] + File.separator
		}
				
		fichier = fichier + packagePathElements[packagePathElements.length-1] + ".class";
				
		File file = new File(fichier);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();

		log.info 'Generation of the binary file gateway interface for receiving event from a messaging middleware: ' + fichier + ' complete successfully.'

		// generate stream handler interface source code

		String streamHandlerClassName = "orcha.lang.generated.StreamHandler"
		s = "." + File.separator + "src" + File.separator + "main" + File.separator + "java"
		path = s + File.separator + streamHandlerClassName + ".java"
		
		log.info 'Generation of the stream handler for receiving event from a messaging middleware: ' + path
		
		codeModel = new JCodeModel();
		
		JDefinedClass streamHandlerClass = codeModel._class(JMod.PUBLIC, streamHandlerClassName, ClassType.CLASS)
		streamHandlerClass.annotate(org.springframework.cloud.stream.annotation.EnableBinding.class).param("value", Sink.class)
		
		jDocComment = streamHandlerClass.javadoc();
		jDocComment.add(String.format("Do not edit this file : auto generated file"));
		
		JFieldVar contextField = streamHandlerClass.field(JMod.PRIVATE, org.springframework.context.ApplicationContext.class, "context")
		contextField.annotate(codeModel.ref(Autowired.class))
		
		method = streamHandlerClass.method(JMod.PUBLIC, streamHandlerClass.owner().VOID, "handle")
	
		if(instruction.springBean instanceof Application) {
			paramType = instruction.springBean.output.adapter.inputEventHandler.input.type
		} else {
			paramType = instruction.springBean.input.type
		}
		
		paramClass = Class.forName(paramType)
		nameParam = method.param(paramClass, "event");
		
		JAnnotationUse annotation = method.annotate(codeModel.ref(org.springframework.cloud.stream.annotation.StreamListener.class))
		annotation.param("value", Sink.INPUT)
			
		JBlock body = method.body()
		
		Class gateWayClass = Class.forName(gateWayClassName)
		
		JVar applicationVar = body.decl(codeModel.ref(gateWayClass), "gateway", JExpr.invoke(contextField, "getBean").arg(codeModel.ref(gateWayClass).dotclass()))
		
		body.add(JExpr.invoke(applicationVar, "call").arg(nameParam))
		
		fileCodeWriter = new FileCodeWriter(new File(s))
		codeModel.build(fileCodeWriter)
		fileCodeWriter.close()

		log.info 'Generation of the stream handler for receiving event from a messaging middleware: ' + path + ' complete successfully.'

		String tt = streamHandlerClassName.replaceAll('\\.', "/")
		
		packagePathElements = tt.split("/");
		fichier = "." + File.separator + "bin" + File.separator + "main" + File.separator
		
		for(int i=0; i<packagePathElements.length-1; i++) {
			fichier = fichier + packagePathElements[i] + File.separator
		}
				
		fichier = fichier + packagePathElements[packagePathElements.length-1] + ".class";

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
		
		paramType = paramType.replaceAll('\\.', "/")
		paramType = "(L" + paramType + ";)V"
			
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "handle", paramType, null, null);
		
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
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, t, "call", paramType, true);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
		
		bytes = cw.toByteArray()
						
		file = new File(fichier);
		fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();

		log.info 'Generation of the stream handler binary file for receiving event from a messaging middleware: ' + fichier + ' complete successfully.'
		
		fichier = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "application.properties"
		
		String destinationName
		
		if(instruction.springBean instanceof Application) {
			destinationName = instruction.springBean.output.adapter.inputEventHandler.name
		} else {
			destinationName = instruction.springBean.name
		}
		
		def lines = []
		
		new File(fichier).eachLine {
			line -> if(line.startsWith("spring.cloud.stream.bindings.input.destination")==false && line.startsWith("# Auto generation of the input")==false && line.equals("")==false) lines.add(line)
		}
		
		new File(fichier).delete()
		
		new File(fichier).withWriter('utf-8') { writer ->
			lines.each{
				writer.writeLine(it)
			}
		}
		
		log.info 'Adding the input binding destination ' + destinationName + ' to: ' + fichier
		
		new File(fichier) << '''

# Auto generation of the input destination to the messaging middleware. Do not delete this line:
spring.cloud.stream.bindings.input.destination=''' + destinationName
		
		log.info 'Adding the input binding destination ' + destinationName + ' to: ' + fichier + ' complete successfully'

		def src = new File(fichier)
		
		fichier = "." + File.separator + "bin" + File.separator + "main" + File.separator + "application.properties"
		
		log.info 'Adding the input binding destination ' + destinationName + ' to: ' + fichier
		
		def dst = new File(fichier)
		dst.delete()
		
		dst << src.text
		
		/*
		
		new File(fichier) << '''

# Auto generation of the input destination to the messaging middleware. Do not delete this line:
spring.cloud.stream.bindings.input.destination=''' + destinationName 
*/		
		log.info 'Adding the input binding destination ' + destinationName + ' to: ' + fichier + ' complete successfully'

	}
			
	public void http(InstructionNode instructionNode) {

		Element rootElement = xmlSpringIntegration.getRootElement()
		
		Instruction instruction = instructionNode.instruction
		def inputChannel = instructionNode.inputName
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
			
		Element channel = new Element("channel", namespace)
		channel.setAttribute("id", inputChannel)
		rootElement.addContent(channel)
		
		namespace = Namespace.getNamespace("int-http", "http://www.springframework.org/schema/integration/http")
		
		HttpAdapter httpAdapter = instruction.springBean.input.adapter
		
		Element adapter = new Element("inbound-channel-adapter", namespace)
		adapter.setAttribute("id", "http-"+inputChannel+"-id")
		adapter.setAttribute("channel", inputChannel)
		adapter.setAttribute("status-code-expression", "T(org.springframework.http.HttpStatus).NO_CONTENT")
		adapter.setAttribute("supported-methods", httpAdapter.method.toString())
		adapter.setAttribute("path", httpAdapter.url)
		
		Input input = instruction.springBean.input		
		adapter.setAttribute("request-payload-type", input.type)
		
		Element requestMapping = new Element("request-mapping", namespace)		
		requestMapping.setAttribute("consumes", input.mimeType)
		adapter.addContent(requestMapping)
		
		rootElement.addContent(adapter)
		
		Element chain = chain(inputChannel, instructionNode.outputName)
		rootElement.addContent(chain)
		
		def id = "headers['id'].toString()"
		Element messageIDEnricher = headerEnricher("messageID", id)
		chain.addContent(messageIDEnricher)
		
	}
	
}
