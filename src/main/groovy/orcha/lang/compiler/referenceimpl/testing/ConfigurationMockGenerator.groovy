package orcha.lang.compiler.referenceimpl.testing

import java.lang.reflect.Method
import java.nio.file.Path
import java.nio.file.Paths
import java.util.List
import com.sun.codemodel.JCodeModel
import com.sun.codemodel.JDefinedClass
import com.sun.codemodel.JDocComment
import com.sun.codemodel.JMethod
import com.sun.codemodel.JMod
import com.sun.codemodel.JVar
import com.sun.codemodel.writer.FileCodeWriter
import com.sun.org.apache.xpath.internal.functions.FuncSubstring
import com.sun.codemodel.ClassType
import com.sun.codemodel.JBlock
import com.sun.codemodel.JClass
import com.sun.codemodel.JExpr
import com.sun.codemodel.JExpression
import com.sun.codemodel.JInvocation
import groovy.util.logging.Slf4j
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.JavaServiceAdapter
import service.order.Order
import service.order.SpecificOrder

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.core.type.filter.AnnotationTypeFilter

import java.util.Hashtable;

@Slf4j
class ConfigurationMockGenerator {
	
	@Autowired
	ApplicationContext context
	
	boolean isMockGenerated
	
	private Map<Class, List<InstructionNode>> getBeansByConfigurationClass(OrchaCodeParser orchaCodeParser){
		
		def beansByConfigurationClass = [:]
		
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
		
		List<InstructionNode> computeNodes = orchaCodeParser.findAllComputeNodes()
		
		List<InstructionNode> receiveNodes = orchaCodeParser.findAllReceiveNodes();		
		
		List<InstructionNode> sendNodes = orchaCodeParser.findAllSendNodes();
		
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
			
			for(Method method: methods){
				InstructionNode instruction = receiveNodes.find{ it.instruction.springBean.input!=null && it.instruction.springBean.input.adapter==null && it.instruction.springBean.name.equals(method.getName())}
				if( instruction != null){
					beans.add(instruction)
				}
			}
			
			for(Method method: methods){
				InstructionNode instruction = sendNodes.find{ it.instruction.springBean.output!=null && it.instruction.springBean.output.adapter==null && it.instruction.springBean.name.equals(method.getName())}
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
		
	boolean generate(OrchaCodeParser orchaCodeParser) throws OrchaConfigurationException{
			
		def beansByConfigurationClass = this.getBeansByConfigurationClass(orchaCodeParser)
		
		if(beansByConfigurationClass.size() == 0){
			isMockGenerated = false
			return false
		}
		
		log.info 'Missing Orcha configuration details for the compilation => auto generate the remainded configuration...'

		beansByConfigurationClass.each{ entry, value ->
			
			Class configurationClass = entry
			
			def instructionNodes = value
						
			instructionNodes.each{ instructionNode ->
				
				Instruction instruction = instructionNode.instruction
										
				if(instruction.springBean instanceof Application){

					/* Generate service interface as java source file
					 
						  Example :
						  
							 public interface OrderConverterService {
								 public SpecificOrder service(Order arg);
							 }
					 
						  from
						 
							 @Bean
							 Application orderConverter(){
								 def program = new OrderConverterApplication(name: "orderConverter", specifications: "bla bla", description: "bla bla")
								 program.input = new Input(type: "service.order.Order")
								 program.output = new Output(type: "service.order.SpecificOrder")
								 return program
							 }
					 */
									 
					 Class<?> beanClass = instruction.springBean.getClass()
								 
					 String packageName =  beanClass.getPackage().getName()
									 
					 JCodeModel codeModel = new JCodeModel();
									 
					 String serviceName = instruction.springBean.name
					 serviceName = serviceName.trim()
					 int indexOfWhiteSpace
					 while((indexOfWhiteSpace=serviceName.indexOf(" ")) != -1){
						 serviceName = serviceName.substring(0, indexOfWhiteSpace).concat(serviceName.substring(indexOfWhiteSpace+1))
					 }
					 
					 String serviceNameInterface = serviceName.substring(0, 1).toUpperCase().concat(serviceName.substring(1)).concat("Service")
					 
					 def serviceClassNameInterface = packageName + '.' + serviceNameInterface
									 
					 JDefinedClass serviceInterface = codeModel._class(JMod.PUBLIC, serviceClassNameInterface, ClassType.INTERFACE)
							 
					 JDocComment jDocComment = serviceInterface.javadoc();
					 jDocComment.add(String.format("Mock of service interface. Auto generated file."));
									 
					 Class serviceArgumentClass = Class.forName(instruction.springBean.input.type)
					 Class serviceReturnClass = Class.forName(instruction.springBean.output.type)
									 
					 JMethod method = serviceInterface.method(JMod.PUBLIC, serviceReturnClass, "service")
					 method.param(serviceArgumentClass, "arg");
									 
					 String s = "." + File.separator + "src" + File.separator + "main" + File.separator + "orcha"
					 FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(s))
					 codeModel.build(fileCodeWriter)
					 fileCodeWriter.close()

					 log.info 'Mock of Java source file service interface ' + serviceClassNameInterface + ' generated into ' + s
									 
					 // generate service interface as java class file (useful to avoid refreshing the IDE like Eclipse)
									 
					 ClassWriter cw = new ClassWriter(0);
					 FieldVisitor fv;
					 MethodVisitor mv;
					 AnnotationVisitor av0;
									 
					 String packagePath = packageName + '.'
					 packagePath = packagePath.replaceAll("\\.", "/");
									 
					 cw.visit(52, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE, packagePath + serviceNameInterface, null, "java/lang/Object", null);
									 
					 String argAndReturn = "(L" + instruction.springBean.input.type + ";)L" + instruction.springBean.output.type + ";"
					 argAndReturn = argAndReturn.replaceAll("\\.", "/")
													 
					 mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, "service", argAndReturn, null, null);
					 mv.visitEnd();
									 
					 cw.visitEnd();
						 
					 byte[] bytes = cw.toByteArray()
									 
					 String[] packagePathElements = packageName.split("\\.");
					 String fichier = "." + File.separator + "bin" + File.separator
					 for(String element: packagePathElements){
						 fichier = fichier + element + File.separator
					 }
									 
					 fichier = fichier + serviceNameInterface + ".class";
									 
					 File file = new File(fichier);
					 FileOutputStream fos = new FileOutputStream(file);
					 fos.write(bytes);
					 fos.close();
					 
					 log.info 'Mock of Java class service interface ' + fichier + ' generated'
					 
				}

				
			}
								
			/* generate mock of the service as java source file
				 Example
					 
					 @Slf4j
					public class SelectbestTVvendorsConfiguration
					extends BenchmarkingVendorsConfiguration
					{
						
						
						@Bean
						public OrderConverterService orderConverterService() {
							OrderConverterService vendorOrderConverter = Mockito.mock((configuration.order.OrderConverterService.class));
							Order order = new Order();
							SpecificOrder specificOrder = new SpecificOrder();
							Mockito.when(vendorOrderConverter.service(order)).thenReturn(specificOrder);
							return vendorOrderConverter;
						}
							
						@Bean
						@Override
						public Application orderConverter() {
							JavaServiceAdapter javaAdapter = new JavaServiceAdapter();
							javaAdapter.setJavaClass("OrderConverterService");
							javaAdapter.setMethod("service");
							Application application = super.orderConverter();
							application.getInput().setAdapter(javaAdapter);
							application.getOutput().setAdapter(javaAdapter);
							return application;
						}
										
					}
			*/
						
			JCodeModel codeModel = new JCodeModel()
							
			String title = orchaCodeParser.getOrchaMetadata().getTitle()
								
			int indexOfWhiteSpace
			
			title = title.trim()
			while((indexOfWhiteSpace=title.indexOf(" ")) != -1){
				title = title.substring(0, indexOfWhiteSpace).concat(title.substring(indexOfWhiteSpace+1))
			}
							
			title = title.substring(0, 1).toUpperCase().concat(title.substring(1)).concat("Configuration")
							
			String packageName =  configurationClass.getPackage().getName()
			
			String serviceMockClassName = packageName + '.' + title
					
			log.info 'Generate a Java source file overriding of the configuration ' + configurationClass + ' :' + serviceMockClassName + ' started...'
			
			JDefinedClass serviceMockClass = codeModel._class(JMod.PUBLIC, serviceMockClassName, ClassType.CLASS)
							
			JDocComment jDocComment = serviceMockClass.javadoc();
			jDocComment.add(String.format("Auto generated configuration file due to missing configuration detail.\nEdit this file to improve the configuration.\nThis file won't be generated again once it has been edited.\nDelete it to generate a new one (any added configuration will be discarded)"))
								
			serviceMockClass.annotate(Configuration.class)
			serviceMockClass.annotate(Slf4j.class)
							
			serviceMockClass._extends(codeModel.ref(configurationClass));
			
			
			log.info 'Generation a Java class overrinding of the configuration ' + configurationClass + ' :' + serviceMockClassName + ' started...'
			
			ClassWriter cw = new ClassWriter(0)
			FieldVisitor fv;
			MethodVisitor mv;
			AnnotationVisitor av0;
			
			cw.visit(52, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, serviceMockClassName.replaceAll("\\.", "/"), null, configurationClass.getCanonicalName().replaceAll("\\.", "/"), null);
			
			av0 = cw.visitAnnotation("Lorg/springframework/context/annotation/Configuration;", true);
			av0.visitEnd();
			
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, configurationClass.getCanonicalName().replaceAll("\\.", "/"), "<init>", "()V", false);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			
			
			
			
				
			instructionNodes.each{ instructionNode ->
				
				Instruction instruction = instructionNode.instruction

				String serviceName = instruction.springBean.name
				serviceName = serviceName.trim()
				
				while((indexOfWhiteSpace=serviceName.indexOf(" ")) != -1){
					serviceName = serviceName.substring(0, indexOfWhiteSpace).concat(serviceName.substring(indexOfWhiteSpace+1))
				}
				
				if(instruction.springBean instanceof Application){
					
					String serviceNameInterface = serviceName.substring(0, 1).toUpperCase().concat(serviceName.substring(1)).concat("Service")
					
					String methodName = serviceNameInterface.substring(0,1).toLowerCase().concat(serviceNameInterface.substring(1))
						
					def serviceClassNameInterface = packageName + '.' + serviceNameInterface
				
					log.info 'Mock of a service => generated method : ' + methodName
					
					Class argumentClass = Class.forName(serviceClassNameInterface)
							
					JMethod method = serviceMockClass.method(JMod.PUBLIC, argumentClass, methodName)
					method.annotate(org.springframework.context.annotation.Bean.class)
									
					JBlock body = method.body();
									
					JInvocation assertEqualsInvoke = codeModel.ref(org.mockito.Mockito.class).staticInvoke("mock").arg(JExpr.direct(serviceClassNameInterface + ".class"));
									
					JVar serviceMockVariable = body.decl(codeModel.ref(argumentClass), methodName + "Mock", assertEqualsInvoke);
									
					argumentClass = Class.forName(instruction.springBean.input.type)
				
					JVar testArgumentVariable = body.decl(codeModel.ref(argumentClass), "mockInput", JExpr._new(codeModel.ref(argumentClass)));
					
					Class returnClass = Class.forName(instruction.springBean.output.type)
				
					JVar testArgumentReturn = body.decl(codeModel.ref(returnClass), "mockOutput", JExpr._new(codeModel.ref(returnClass)));
									
					JInvocation testInvoke = codeModel.ref(org.mockito.Mockito.class).staticInvoke("when").arg( serviceMockVariable.invoke("service").arg(testArgumentVariable)).invoke("thenReturn").arg(testArgumentReturn);
					body.add(testInvoke);
									
					body._return(serviceMockVariable)
					
					
					
					mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, "()L" + serviceClassNameInterface.replaceAll("\\.", "/") + ";", null, null);
					
					av0 = mv.visitAnnotation("Lorg/springframework/context/annotation/Bean;", true);
					av0.visitEnd();
					
					mv.visitCode();
					mv.visitLdcInsn(Type.getType("L" + serviceClassNameInterface.replaceAll("\\.", "/") + ";"));
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/mockito/Mockito", "mock", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
					mv.visitTypeInsn(Opcodes.CHECKCAST, serviceClassNameInterface.replaceAll("\\.", "/"));
					mv.visitVarInsn(Opcodes.ASTORE, 1);
					mv.visitTypeInsn(Opcodes.NEW, argumentClass.getCanonicalName().replaceAll("\\.", "/"));
					mv.visitInsn(Opcodes.DUP);
					mv.visitMethodInsn(Opcodes.INVOKESPECIAL, argumentClass.getCanonicalName().replaceAll("\\.", "/"), "<init>", "()V", false);
					mv.visitVarInsn(Opcodes.ASTORE, 2);
					mv.visitTypeInsn(Opcodes.NEW, returnClass.getCanonicalName().replaceAll("\\.", "/"));
					mv.visitInsn(Opcodes.DUP);
					mv.visitMethodInsn(Opcodes.INVOKESPECIAL, returnClass.getCanonicalName().replaceAll("\\.", "/"), "<init>", "()V", false);
					mv.visitVarInsn(Opcodes.ASTORE, 3);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitVarInsn(Opcodes.ALOAD, 2);
					mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, serviceClassNameInterface.replaceAll("\\.", "/"), "service", "(L" + argumentClass.getCanonicalName().replaceAll("\\.", "/") + ";)L" + returnClass.getCanonicalName().replaceAll("\\.", "/") + ";", true);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/mockito/Mockito", "when", "(Ljava/lang/Object;)Lorg/mockito/stubbing/OngoingStubbing;", false);
					mv.visitVarInsn(Opcodes.ALOAD, 3);
					mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/mockito/stubbing/OngoingStubbing", "thenReturn", "(Ljava/lang/Object;)Lorg/mockito/stubbing/OngoingStubbing;", true);
					mv.visitInsn(Opcodes.POP);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitInsn(Opcodes.ARETURN);
					mv.visitMaxs(2, 4);
					mv.visitEnd();
					
						
						
						
						
					
								
					methodName = serviceName.substring(0,1).toLowerCase().concat(serviceName.substring(1))
					
					log.info 'Mock of a service => override the Orcha configuration for the application ' + methodName
									
					method = serviceMockClass.method(JMod.PUBLIC, orcha.lang.configuration.Application.class, methodName)
					method.annotate(org.springframework.context.annotation.Bean.class)
					method.annotate(java.lang.Override.class)
									
					body = method.body();
								
					JVar adapterJVar = body.decl(codeModel.ref(orcha.lang.configuration.JavaServiceAdapter.class), "javaAdapter", JExpr._new(codeModel.ref(orcha.lang.configuration.JavaServiceAdapter)));
						
					JInvocation jInvoque = adapterJVar.invoke("setJavaClass").arg(JExpr.lit(serviceNameInterface));
					body.add(jInvoque);
														
					jInvoque = adapterJVar.invoke("setMethod").arg(JExpr.lit("service"));
					body.add(jInvoque);
				
					jInvoque = JExpr._super().invoke(instruction.springBean.name);
					JVar jvar = body.decl(codeModel.ref(orcha.lang.configuration.Application.class), "application", jInvoque);
									
					jInvoque = jvar.invoke("getInput").invoke("setAdapter").arg(adapterJVar);
					body.add(jInvoque);
									
					jInvoque = jvar.invoke("getOutput").invoke("setAdapter").arg(adapterJVar);
					body.add(jInvoque);
									
					body._return(jvar)
					
					
					
					mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, "()Lorcha/lang/configuration/Application;", null, null);
					
					av0 = mv.visitAnnotation("Lorg/springframework/context/annotation/Bean;", true);
					av0.visitEnd();
					
					mv.visitCode();
					mv.visitTypeInsn(Opcodes.NEW, "orcha/lang/configuration/JavaServiceAdapter");
					mv.visitInsn(Opcodes.DUP);
					mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "orcha/lang/configuration/JavaServiceAdapter", "<init>", "()V", false);
					mv.visitVarInsn(Opcodes.ASTORE, 1);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitLdcInsn(serviceNameInterface);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/JavaServiceAdapter", "setJavaClass", "(Ljava/lang/Object;)V", false);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitLdcInsn("service");
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/JavaServiceAdapter", "setMethod", "(Ljava/lang/Object;)V", false);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitMethodInsn(Opcodes.INVOKESPECIAL, configurationClass.getCanonicalName().replaceAll("\\.", "/"), methodName, "()Lorcha/lang/configuration/Application;", false);
					mv.visitVarInsn(Opcodes.ASTORE, 2);
					mv.visitVarInsn(Opcodes.ALOAD, 2);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/Application", "getInput", "()Lorcha/lang/configuration/Input;", false);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/Input", "setAdapter", "(Ljava/lang/Object;)V", false);
					mv.visitVarInsn(Opcodes.ALOAD, 2);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/Application", "getOutput", "()Lorcha/lang/configuration/Output;", false);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/Output", "setAdapter", "(Lorcha/lang/configuration/ConfigurableProperties;)V", false);
					mv.visitVarInsn(Opcodes.ALOAD, 2);
					mv.visitInsn(Opcodes.ARETURN);
					mv.visitMaxs(2, 3);
					mv.visitEnd();
					
						
								
				} else if(instruction.springBean instanceof EventHandler){
				
					String methodName = serviceName.substring(0,1).toLowerCase().concat(serviceName.substring(1))
					
					log.info 'Mock of an event handler => override the Orcha configuration event handler for ' + methodName
									
					JMethod method = serviceMockClass.method(JMod.PUBLIC, orcha.lang.configuration.EventHandler.class, methodName)
					method.annotate(org.springframework.context.annotation.Bean.class)
					method.annotate(java.lang.Override.class)
									
					JBlock body = method.body();
					
					JVar adapterJVar
					
					if(instruction.springBean.input!=null && instruction.springBean.input.adapter==null){						
						adapterJVar = body.decl(codeModel.ref(orcha.lang.configuration.InputFileAdapter.class), "localFileAdapter", JExpr._new(codeModel.ref(orcha.lang.configuration.InputFileAdapter)));
					} else {
						adapterJVar = body.decl(codeModel.ref(orcha.lang.configuration.OutputFileAdapter.class), "localFileAdapter", JExpr._new(codeModel.ref(orcha.lang.configuration.OutputFileAdapter)));
					}
										
					// example: C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/OrchaBeforeLibrary/bin/data/order
					// the corresponding src folder is src/main/resources/data/order
					
					String dataFolder = "" 	
					
					String folder = packageName.substring( "configuration.".length() )
					
					String[] dataSubFolders = folder.split("\\.")
					if(dataSubFolders.size() > 0){
						for(String s: dataSubFolders){
							dataFolder =  dataFolder + File.separator + s
						}
					} else {
						dataFolder =  dataFolder + File.separator + folder
					}
					
					dataFolder =  dataFolder + File.separator + methodName
					
					// src/main/resources 
					  
					Path path = Paths.get(this.getClass().getClassLoader().getResource("data").toURI())
					
					dataFolder = Paths.get(path.toString().concat(dataFolder)).toUri().toURL().getPath().substring(1)
					
					if(instruction.springBean.input!=null && instruction.springBean.input.adapter==null){
						log.info "Mock of an event handler => put your input data into (src/main/resources): " + dataFolder
					} else {
						log.info "Mock of an event handler => output data will be into: " + dataFolder
					}					
					
					JInvocation jInvoque = adapterJVar.invoke("setDirectory").arg(JExpr.lit(dataFolder))
					body.add(jInvoque)
					
					if(instruction.springBean.output!=null && instruction.springBean.output.adapter==null){
						
						jInvoque = adapterJVar.invoke("setCreateDirectory").arg(JExpr.lit(true))
						body.add(jInvoque)
						
						jInvoque = adapterJVar.invoke("setAppendNewLine").arg(JExpr.lit(true))
						body.add(jInvoque)
						
						jInvoque = adapterJVar.invoke("setWritingMode").arg(JExpr.direct("orcha.lang.configuration.OutputFileAdapter.WritingMode.APPEND"))
						body.add(jInvoque)
					}
					
					jInvoque = JExpr._super().invoke(instruction.springBean.name);
					JVar jvar = body.decl(codeModel.ref(orcha.lang.configuration.EventHandler.class), "eventHandler", jInvoque);
					
					if(instruction.springBean.input!=null && instruction.springBean.input.adapter==null){
						jInvoque = jvar.invoke("getInput").invoke("setAdapter").arg(adapterJVar);
					} else {
						jInvoque = jvar.invoke("getOutput").invoke("setAdapter").arg(adapterJVar);
					}					
					
					body.add(jInvoque);
												
					body._return(jvar)
					
					
					if(instruction.springBean.input!=null && instruction.springBean.input.adapter==null){
						mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, "()Lorcha/lang/configuration/EventHandler;", null, null);
					
						av0 = mv.visitAnnotation("Lorg/springframework/context/annotation/Bean;", true);
						av0.visitEnd();
						
						mv.visitCode();
						mv.visitTypeInsn(Opcodes.NEW, "orcha/lang/configuration/InputFileAdapter");
						mv.visitInsn(Opcodes.DUP);
						mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "orcha/lang/configuration/InputFileAdapter", "<init>", "()V", false);
						mv.visitVarInsn(Opcodes.ASTORE, 1);
						mv.visitVarInsn(Opcodes.ALOAD, 1);
						mv.visitLdcInsn(dataFolder);
						
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/InputFileAdapter", "setDirectory", "(Ljava/lang/String;)V", false);
						mv.visitVarInsn(Opcodes.ALOAD, 0);
						mv.visitMethodInsn(Opcodes.INVOKESPECIAL, configurationClass.getCanonicalName().replaceAll("\\.", "/"), methodName, "()Lorcha/lang/configuration/EventHandler;", false);
											
						mv.visitVarInsn(Opcodes.ASTORE, 2);
						mv.visitVarInsn(Opcodes.ALOAD, 2);
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/EventHandler", "getInput", "()Lorcha/lang/configuration/Input;", false);
						mv.visitVarInsn(Opcodes.ALOAD, 1);
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/Input", "setAdapter", "(Ljava/lang/Object;)V", false);
						mv.visitVarInsn(Opcodes.ALOAD, 2);
						mv.visitInsn(Opcodes.ARETURN);
						mv.visitMaxs(2, 3);
						mv.visitEnd();						
					
					} 
					
					if(instruction.springBean.output!=null && instruction.springBean.output.adapter==null){
						
						mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, "()Lorcha/lang/configuration/EventHandler;", null, null);
						
						av0 = mv.visitAnnotation("Lorg/springframework/context/annotation/Bean;", true);
						av0.visitEnd();
						
						mv.visitCode();
						mv.visitTypeInsn(Opcodes.NEW, "orcha/lang/configuration/OutputFileAdapter");
						mv.visitInsn(Opcodes.DUP);
						mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "orcha/lang/configuration/OutputFileAdapter", "<init>", "()V", false);
						mv.visitVarInsn(Opcodes.ASTORE, 1);
						mv.visitVarInsn(Opcodes.ALOAD, 1);
						mv.visitLdcInsn(dataFolder);
						
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/OutputFileAdapter", "setDirectory", "(Ljava/lang/String;)V", false);									
						mv.visitVarInsn(Opcodes.ALOAD, 1);
						mv.visitInsn(Opcodes.ICONST_1);
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/OutputFileAdapter", "setCreateDirectory", "(Z)V", false);
						mv.visitVarInsn(Opcodes.ALOAD, 1);
						mv.visitInsn(Opcodes.ICONST_1);
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/OutputFileAdapter", "setAppendNewLine", "(Z)V", false);
						//mv.visitVarInsn(Opcodes.ALOAD, 1);
						//mv.visitFieldInsn(Opcodes.GETSTATIC, "orcha/lang/configuration/OutputFileAdapter$WritingMode", "APPEND", "Lorcha/lang/configuration/OutputFileAdapter$WritingMode;");
						//mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/OutputFileAdapter", "setWritingMode", "(Lorcha/lang/configuration/OutputFileAdapter$WritingMode;)V", false);
							
						mv.visitVarInsn(Opcodes.ALOAD, 0);
						mv.visitMethodInsn(Opcodes.INVOKESPECIAL, configurationClass.getCanonicalName().replaceAll("\\.", "/"), methodName, "()Lorcha/lang/configuration/EventHandler;", false);											
						mv.visitVarInsn(Opcodes.ASTORE, 2);
						mv.visitVarInsn(Opcodes.ALOAD, 2);

						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/EventHandler", "getOutput", "()Lorcha/lang/configuration/Output;", false);
						mv.visitVarInsn(Opcodes.ALOAD, 1);
						
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "orcha/lang/configuration/Output", "setAdapter", "(Lorcha/lang/configuration/ConfigurableProperties;)V", false);
						mv.visitVarInsn(Opcodes.ALOAD, 2);
						mv.visitInsn(Opcodes.ARETURN);
						mv.visitMaxs(2, 3);
						mv.visitEnd();
						
						
					}
					
										

				}						
				
				String s = "." + File.separator + "src" + File.separator + "main" + File.separator + "orcha"
				FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(s))
				codeModel.build(fileCodeWriter)
				fileCodeWriter.close()
				
				log.info 'Overriding of the configuration ' + configurationClass + ' (' + serviceMockClassName + ' source file) generated into: ' + s
			}
			
			cw.visitEnd();
			
			byte[] bytes = cw.toByteArray()
							
			String[] packagePathElements = serviceMockClassName.split("\\.");
			String fichier = "." + File.separator + "bin"
			for(String element: packagePathElements){
				fichier = fichier + File.separator + element
			}
										
			File file = new File(fichier + ".class");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.close();

			log.info 'Overriding of the configuration ' + configurationClass + ' (' + serviceMockClassName + ' class file) generated into: ' + fichier + ".class"
		}
		
		log.info 'Missing Orcha configuration details for the compilation => auto generation of the remainded configuration achived successfully.'
		
		isMockGenerated = true
		return true		
	}

}
