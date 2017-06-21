package orcha.lang.compiler.serviceOffer

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
import com.sun.codemodel.JExpr
import com.sun.codemodel.JInvocation
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import java.lang.reflect.Method
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.configuration.Application
import orcha.lang.configuration.Input
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.ScriptServiceAdapter

import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.filter.AnnotationTypeFilter

/**
 * An ASM version of Java classes can be generated using (ASM 5.2) :
 * java -cp asm-util-5.2.jar;ams-5.2.jar org.objectweb.asm.util.ASMifier *.class > *.java 
 * where * is a Java class.
 * 
 * @author Ben C.
 *
 */
@Slf4j
class ServiceOfferSelectionGenerator {
	
	String registryURI = "http://localhost:8080/"
		
	boolean generate(OrchaCodeParser orchaCodeParser){
		
		//def beansByConfigurationClass = this.getBeansByConfigurationClass(orchaCodeParser)
		
		def beansByConfigurationClass = orchaCodeParser.findAllComputeNodesWithoutAdapterByConfigurationClass()
		
		if(beansByConfigurationClass.size() == 0){
			return false
		}
		
		log.info 'Missing Orcha configuration details for the compilation => auto generate a service selector...'
		
		def offersByapplication = [:]
		
		// get all the applications (application offers) from the registry having the same input type and output type as all the applications without adapter		
		
		beansByConfigurationClass.each{ entry, value ->
			
			Class configurationClass = entry
			
			def instructionNodes = value
						
			instructionNodes.each{ instructionNode ->
				
				Instruction instruction = instructionNode.instruction
										
				if(instruction.springBean instanceof Application){
					
					def applicationOffers = []
					
					Application application = (Application)instruction.springBean
					
					String inputType = application.input.type
					String outputType = application.output.type
					
					String uri = registryURI + "applications?input=" + inputType + "&output=" + outputType
			
					def http = new HTTPBuilder(uri)
					
					http.request(groovyx.net.http.Method.GET,ContentType.JSON) { req ->
						response.success = { resp, apps  ->
							apps.each{ appli ->				
								
								//def input = new Input(appli.input)
								
								def adapter
								
								if(appli.language.equalsIgnoreCase("java") || appli.language.equalsIgnoreCase("groovy")){
									adapter = new JavaServiceAdapter(appli.output.adapter)
								} else if(appli.language.equalsIgnoreCase("js") || appli.language.equalsIgnoreCase("javascript") || appli.language.equalsIgnoreCase("java script")){
									adapter = new ScriptServiceAdapter(appli.output.adapter)
								}
										
								def input = new Input(mimeType: appli.input.mimeType, type: appli.input.type, value: appli.input.value, adapter: adapter, autoStartup: appli.input.autoStartup)
								def output = new Output(mimeType: appli.output.mimeType, type: appli.output.type, value: appli.output.value, adapter: adapter, autoStartup: appli.output.autoStartup)
													
								def app = new Application(specifications: appli.specifications, name: appli.name, description: appli.description, language: appli.language, properties: appli.properties, input: input, output: output, state: appli.state, error: appli.error)
								
								applicationOffers.add(app)
							}
						   
						}
						response.failure = { resp ->
							throw new Exception("Error: ${resp.status}")
						}
					}
				
					offersByapplication.put(application, applicationOffers)
				}								
			}
		}
		
		
		// unzip the project for service offers selection
		// generation of an Orcha source file for the service offers selection
		// generation of a Groovy source file for the configuration of the Orcha offers selection program
		// for each service offer:
		//		generation of an application's bean for the current offer
		// generation of an application's bean for the selection of the offer
		// generation of an input event handler
		// generation of an output event handler
		// download services file for all the service offers
		
		offersByapplication.each { application, offers ->
		
			// unzip the project for service offers selection
			
			String projectTemplateFile = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "orchaProjectTemplate.zip"
			String outputFolder = "orchaProjects"
			
			ZipInputStream zis =  new ZipInputStream(new FileInputStream(projectTemplateFile))
			
			ZipEntry ze = zis.getNextEntry()
			
			File fileEntry
			byte[] buffer = new byte[1024]
			
			while(ze!=null){
				
				fileEntry = new File(outputFolder + File.separator + ze.getName()) 
				
				if(ze.isDirectory()){
					fileEntry.mkdirs()
				} else {
					
					new File(fileEntry.getParent()).mkdirs()
					
					try{
						FileOutputStream fos = new FileOutputStream(fileEntry);
						int len;
						while ((len = zis.read(buffer)) > 0) {
						   fos.write(buffer, 0, len);
						}
						fos.close()
					}catch(FileNotFoundException e){
					}
					
				}
	
				ze = zis.getNextEntry();
			}
			
			zis.closeEntry();
			zis.close();
			
			// generation of an Orcha source file for the service offers selection 
			
			String inputEventHandler
			String outputEventHandler
			
			String send = application.name
			
			String className = application.name.substring(0,1).toUpperCase().concat(application.name.substring(1))
			
			String pathToOrchaComparisonFile = outputFolder + File.separator + "src" + File.separator + "main" + File.separator + "orcha" + File.separator + "source" + File.separator + application.name 
			
			new File(pathToOrchaComparisonFile).mkdirs()
			
			pathToOrchaComparisonFile = pathToOrchaComparisonFile +  File.separator + "Select" + className + ".groovy"
			
			log.info 'Missing Orcha configuration details for the compilation => orcha program service selector generated: ' + new File(pathToOrchaComparisonFile).getAbsolutePath()
			
			new File(pathToOrchaComparisonFile).withWriter('utf-8') { writer ->
				String event
				int indexOfDot = application.input.type.lastIndexOf(".")
				if(indexOfDot != -1){
					event = application.input.type.substring(indexOfDot + 1)
				} else {
					event = application.input.type
				}
				event = event.toLowerCase()
				
				inputEventHandler = event + "EventHandler"
							
				String packageName = "package source." + application.name
				writer.writeLine packageName
				
				String title = "title \"select" + className + "\""
				writer.writeLine title
				
				String comparison = "compute select" + className + " with "
				
				String when = "when \"select" + className + " terminates\""
				
				outputEventHandler = send + "Output"
				
				send = "send select" + className + ".result to " + outputEventHandler
				
				className = className.concat("OffersComparison")
				
				String synchronize = "when \"("
				
				int i = 0
				
				offers.each{ offer ->
					writer.writeLine "receive " + event + " from " + inputEventHandler
					writer.writeLine "compute " + offer.name + " with " + event + ".value"
					synchronize = synchronize + offer.name + " terminates)"
					comparison = comparison + offer.name + ".result"
					i++
					if(i < offers.size()){
						synchronize = synchronize + " and ("
						comparison = comparison + ", "
					}
				}
				synchronize = synchronize + "\""
				writer.writeLine synchronize
	
				writer.writeLine comparison
				
				writer.writeLine when
				
				writer.writeLine send
			}
			
			
			
			// generation of a Groovy source file for the configuration of the Orcha offers selection program 
			
			JCodeModel codeModel = new JCodeModel()
			
			String title = orchaCodeParser.getOrchaMetadata().getTitle()
							
			int indexOfWhiteSpace
			
			title = title.trim()
			while((indexOfWhiteSpace=title.indexOf(" ")) != -1){
			title = title.substring(0, indexOfWhiteSpace).concat(title.substring(indexOfWhiteSpace+1))
			}
						
			title = title.substring(0, 1).toUpperCase().concat(title.substring(1)).concat("Configuration")
				
			String serviceMockClassName = title
			
			String packageName = "configuration." + application.name
			
			String configurationClass = application.name.substring(0,1).toUpperCase().concat(application.name.substring(1)) + 'Configuration'
			
			String serviceMockFullClassName = packageName + '.' + configurationClass
				
			log.info 'Generate a Groovy source file overriding of the configuration ' + configurationClass + ' :' + serviceMockFullClassName + ' started...'
			
			JDefinedClass serviceMockClass = codeModel._class(JMod.PUBLIC, serviceMockFullClassName, ClassType.CLASS)
						
			JDocComment jDocComment = serviceMockClass.javadoc();
			jDocComment.add(String.format("Auto generated configuration file due to missing configuration detail.\nEdit this file to improve the configuration.\nThis file won't be generated again once it has been edited.\nDelete it to generate a new one (any added configuration will be discarded)"))
							
			serviceMockClass.annotate(Configuration.class)
			serviceMockClass.annotate(Slf4j.class)
			
			JMethod method
			JBlock body
			JVar jVar
			JInvocation jInvoque
			
			offers.each{ offer ->
				
				// generation of an application's bean for the current offer
				
				method = serviceMockClass.method(JMod.PUBLIC, orcha.lang.configuration.Application.class, offer.name)
				method.annotate(org.springframework.context.annotation.Bean.class)
								
				body = method.body()
				
				jVar = body.decl(codeModel.ref(orcha.lang.configuration.Application.class), "application", JExpr._new(codeModel.ref(orcha.lang.configuration.Application)))
				
				jInvoque = jVar.invoke("setName").arg(JExpr.lit(offer.name))
				body.add(jInvoque)
				
				jInvoque = jVar.invoke("setLanguage").arg(JExpr.lit(offer.language))
				body.add(jInvoque)
				
				if(offer.description != null){
					jInvoque = jVar.invoke("setDescription").arg(JExpr.lit(offer.description))
					body.add(jInvoque)
				}
				
				if(offer.specifications != null){
					jInvoque = jVar.invoke("setSpecifications").arg(JExpr.lit(offer.specifications))
					body.add(jInvoque)
				}
				
				if(offer.language.equalsIgnoreCase("java") || offer.language.equalsIgnoreCase("groovy")){
					
					JVar adapterJVar = body.decl(codeModel.ref(orcha.lang.configuration.JavaServiceAdapter.class), "javaAdapter", JExpr._new(codeModel.ref(orcha.lang.configuration.JavaServiceAdapter)));
					
					jInvoque = adapterJVar.invoke("setJavaClass").arg(JExpr.lit(offer.input.adapter.javaClass))
					body.add(jInvoque);
														
					jInvoque = adapterJVar.invoke("setMethod").arg(JExpr.lit(offer.input.adapter.method))
					body.add(jInvoque);
							
					JVar inputTypeJVar = body.decl(codeModel.ref(orcha.lang.configuration.Input.class), "input", JExpr._new(codeModel.ref(orcha.lang.configuration.Input)));
					
					jInvoque = inputTypeJVar.invoke("setType").arg(JExpr.lit(offer.input.type))
					body.add(jInvoque)
					
					if(offer.input.mimeType != null){
						jInvoque = inputTypeJVar.invoke("setMimeType").arg(JExpr.lit(offer.input.mimeType))
						body.add(jInvoque)
					}
					
					jInvoque = inputTypeJVar.invoke("setAdapter").arg(adapterJVar);
					body.add(jInvoque)
					
					jInvoque = jVar.invoke("setInput").arg(inputTypeJVar)
					body.add(jInvoque)
				
					JVar outputTypeJVar = body.decl(codeModel.ref(orcha.lang.configuration.Output.class), "output", JExpr._new(codeModel.ref(orcha.lang.configuration.Output)));
					
					jInvoque = outputTypeJVar.invoke("setType").arg(JExpr.lit(offer.output.type))
					body.add(jInvoque);
					
					jInvoque = outputTypeJVar.invoke("setAdapter").arg(adapterJVar);
					body.add(jInvoque)
					
					jInvoque = jVar.invoke("setOutput").arg(outputTypeJVar)
					body.add(jInvoque)	
									
				} else if(offer.language.equalsIgnoreCase("js") || offer.language.equalsIgnoreCase("javascript") || offer.language.equalsIgnoreCase("java script")){
				
					JVar adapterJVar = body.decl(codeModel.ref(orcha.lang.configuration.ScriptServiceAdapter.class), "scriptAdapter", JExpr._new(codeModel.ref(orcha.lang.configuration.ScriptServiceAdapter)));
					
					jInvoque = adapterJVar.invoke("setFile").arg(JExpr.lit(offer.input.adapter.file))
					body.add(jInvoque);
															
					JVar inputTypeJVar = body.decl(codeModel.ref(orcha.lang.configuration.Input.class), "input", JExpr._new(codeModel.ref(orcha.lang.configuration.Input)));
					
					jInvoque = inputTypeJVar.invoke("setType").arg(JExpr.lit(offer.input.type))
					body.add(jInvoque)
					
					if(offer.input.mimeType != null){
						jInvoque = inputTypeJVar.invoke("setMimeType").arg(JExpr.lit(offer.input.mimeType))
						body.add(jInvoque)
					}
					
					jInvoque = inputTypeJVar.invoke("setAdapter").arg(adapterJVar);
					body.add(jInvoque)
					
					jInvoque = jVar.invoke("setInput").arg(inputTypeJVar)
					body.add(jInvoque)
				
					JVar outputTypeJVar = body.decl(codeModel.ref(orcha.lang.configuration.Output.class), "output", JExpr._new(codeModel.ref(orcha.lang.configuration.Output)));
					
					jInvoque = outputTypeJVar.invoke("setType").arg(JExpr.lit(offer.output.type))
					body.add(jInvoque);
					
					jInvoque = outputTypeJVar.invoke("setAdapter").arg(adapterJVar);
					body.add(jInvoque)
					
					jInvoque = jVar.invoke("setOutput").arg(outputTypeJVar)
					body.add(jInvoque)		
					
				}
				
				body._return(jVar)
				
			}
			
			offers.each{ offer ->
				
				if(offer.language.equalsIgnoreCase("java") || offer.language.equalsIgnoreCase("groovy")){
					
					
					String serviceBeanName = offer.input.adapter.javaClass
					serviceBeanName = serviceBeanName.substring(serviceBeanName.lastIndexOf('.')+1)
					serviceBeanName = serviceBeanName.substring(0,1).toLowerCase() + serviceBeanName.substring(1)
					
					Class argumentClass = Class.forName(offer.input.adapter.javaClass)
					
					method = serviceMockClass.method(JMod.PUBLIC, argumentClass, serviceBeanName)
					method.annotate(org.springframework.context.annotation.Bean.class)
			
					body = method.body()
					
					JVar beanJVar = body.decl(codeModel.ref(argumentClass), "service", JExpr._new(codeModel.ref(argumentClass.getCanonicalName())))
					
					body._return(beanJVar)
					
				}
			}
			
			
			
			// generation of an application's bean for the selection of the offer
			
			String offerSelectionMethodsName = "select" + application.name.substring(0,1).toUpperCase().concat(application.name.substring(1))
			
			method = serviceMockClass.method(JMod.PUBLIC, orcha.lang.configuration.Application.class, offerSelectionMethodsName)
			method.annotate(org.springframework.context.annotation.Bean.class)
							
			body = method.body()
			
			jVar = body.decl(codeModel.ref(orcha.lang.configuration.Application.class), "application", JExpr._new(codeModel.ref(orcha.lang.configuration.Application)))
			
			jInvoque = jVar.invoke("setName").arg(JExpr.lit(offerSelectionMethodsName))
			body.add(jInvoque)
			
			jInvoque = jVar.invoke("setDescription").arg(JExpr.lit("selection of a service offer"))
			body.add(jInvoque)
			
			JVar inputTypeJVar = body.decl(codeModel.ref(orcha.lang.configuration.Input.class), "input", JExpr._new(codeModel.ref(orcha.lang.configuration.Input)));
			
			String type = "java.util.List<" + application.output.type + ">"
			jInvoque = inputTypeJVar.invoke("setType").arg(JExpr.lit(type))
			body.add(jInvoque)
			
			jInvoque = jVar.invoke("setInput").arg(inputTypeJVar)
			body.add(jInvoque)
		
			JVar outputTypeJVar = body.decl(codeModel.ref(orcha.lang.configuration.Output.class), "output", JExpr._new(codeModel.ref(orcha.lang.configuration.Output)));
			
			jInvoque = outputTypeJVar.invoke("setType").arg(JExpr.lit(application.output.type))
			body.add(jInvoque);
			
			jInvoque = jVar.invoke("setOutput").arg(outputTypeJVar)
			body.add(jInvoque)
			
			body._return(jVar)
			
			
			
			
			// generation of an input event handler
						
			method = serviceMockClass.method(JMod.PUBLIC, orcha.lang.configuration.EventHandler.class, inputEventHandler)
			method.annotate(org.springframework.context.annotation.Bean.class)
							
			body = method.body()
			
			JVar eventHandlerJVar = body.decl(codeModel.ref(orcha.lang.configuration.EventHandler.class), "eventHandler", JExpr._new(codeModel.ref(orcha.lang.configuration.EventHandler)))
			
			jInvoque = eventHandlerJVar.invoke("setName").arg(JExpr.lit(inputEventHandler))
			body.add(jInvoque)
			
			JVar adapterJVar = body.decl(codeModel.ref(orcha.lang.configuration.InputFileAdapter.class), "localFileAdapter", JExpr._new(codeModel.ref(orcha.lang.configuration.InputFileAdapter)))			
								
			String dataFolder = outputFolder + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "data" + File.separator + application.name + File.separator + inputEventHandler
			
			File file = new File(dataFolder)
			file.mkdirs()
			
			dataFolder =  file.getAbsoluteFile()
			dataFolder = dataFolder.replaceAll("\\\\", "/")
 				
			log.info "Generate default input event handler for " + application.name + " as an InputFileAdapter => put your input data into : " + dataFolder			
			
			jInvoque = adapterJVar.invoke("setDirectory").arg(JExpr.lit(dataFolder))
			body.add(jInvoque)
			
			inputTypeJVar = body.decl(codeModel.ref(orcha.lang.configuration.Input.class), "input", JExpr._new(codeModel.ref(orcha.lang.configuration.Input)));
					
			jInvoque = inputTypeJVar.invoke("setType").arg(JExpr.lit(application.input.type))
			body.add(jInvoque)
					
			if(application.input.mimeType != null){
				jInvoque = inputTypeJVar.invoke("setMimeType").arg(JExpr.lit(application.input.mimeType))
				body.add(jInvoque)
			} else {
				jInvoque = inputTypeJVar.invoke("setMimeType").arg(JExpr.lit("application/json"))
				body.add(jInvoque)
			}
			
			jInvoque = inputTypeJVar.invoke("setAdapter").arg(adapterJVar);
			body.add(jInvoque)
			
			jInvoque = eventHandlerJVar.invoke("setInput").arg(inputTypeJVar)
			body.add(jInvoque)
										
			body._return(eventHandlerJVar)
			
			
			
			// generation of an output event handler
			
			method = serviceMockClass.method(JMod.PUBLIC, orcha.lang.configuration.EventHandler.class, outputEventHandler)
			method.annotate(org.springframework.context.annotation.Bean.class)
			//method.annotate(java.lang.Override.class)
							
			body = method.body()
			
			eventHandlerJVar = body.decl(codeModel.ref(orcha.lang.configuration.EventHandler.class), "eventHandler", JExpr._new(codeModel.ref(orcha.lang.configuration.EventHandler)))
			
			jInvoque = eventHandlerJVar.invoke("setName").arg(JExpr.lit(outputEventHandler))
			body.add(jInvoque)
			
			adapterJVar = body.decl(codeModel.ref(orcha.lang.configuration.OutputFileAdapter.class), "localFileAdapter", JExpr._new(codeModel.ref(orcha.lang.configuration.OutputFileAdapter)));			
								
			dataFolder = outputFolder + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "data" + File.separator + application.name + File.separator + outputEventHandler
			
			file = new File(dataFolder)
			file.mkdirs()
			
			dataFolder =  file.getAbsoluteFile()
			dataFolder = dataFolder.replaceAll("\\\\", "/")
			
			log.info "Generate default output event handler for " + application.name + " as an OutputFileAdapter => get the output data into : " + dataFolder
			
			jInvoque = adapterJVar.invoke("setDirectory").arg(JExpr.lit(dataFolder))
			body.add(jInvoque)
			
			jInvoque = adapterJVar.invoke("setCreateDirectory").arg(JExpr.lit(true))
			body.add(jInvoque)
				
			jInvoque = adapterJVar.invoke("setAppendNewLine").arg(JExpr.lit(true))
			body.add(jInvoque)
				
			jInvoque = adapterJVar.invoke("setWritingMode").arg(JExpr.direct("orcha.lang.configuration.OutputFileAdapter.WritingMode.APPEND"))
			body.add(jInvoque)
			
			outputTypeJVar = body.decl(codeModel.ref(orcha.lang.configuration.Output.class), "output", JExpr._new(codeModel.ref(orcha.lang.configuration.Output)));
					
			jInvoque = outputTypeJVar.invoke("setType").arg(JExpr.lit(application.output.type))
			body.add(jInvoque)
					
			if(application.output.mimeType != null){
				jInvoque = outputTypeJVar.invoke("setMimeType").arg(JExpr.lit(application.output.mimeType))
				body.add(jInvoque)
			}
			
			jInvoque = outputTypeJVar.invoke("setAdapter").arg(adapterJVar);
			body.add(jInvoque)
			
			jInvoque = eventHandlerJVar.invoke("setOutput").arg(outputTypeJVar)
			body.add(jInvoque)
										
			body._return(eventHandlerJVar)
			
			
			
			String s = outputFolder + File.separator + "src" + File.separator + "main" + File.separator + "orcha"
			FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(s))
			codeModel.build(fileCodeWriter)
			fileCodeWriter.close()
			
			
			
			
			String[] packageElements = serviceMockFullClassName.split("\\.");
			for(String element: packageElements){
				s = s + File.separator + element 
			}
			
			String javaFileName = s + ".java"
			
			def linesInJavaFile = []

			new File(javaFileName).eachLine { line ->
				linesInJavaFile.add(line)
			}
			
			new File(javaFileName).delete()
			
			String groovyFileName = s + ".groovy"
			 
			new File(groovyFileName).withWriter('utf-8') { writer ->
				linesInJavaFile.each{ line ->
					writer.writeLine line
				}
			}
	
			
			
			
			// download services file for all the service offers
			
			offers.each{ offer ->
				
				String content = downloadService(offer.name, offer.input.type)
				
				String[] packagePathElements = offer.input.type.split("\\.");
				String fichier = outputFolder + File.separator + "src" + File.separator + "main" + File.separator + "orcha" + File.separator
				for(String element: packagePathElements){
					fichier = fichier + element + File.separator
				}
					
				String fileName = fichier.substring(0, fichier.size()-1)
				
				if(offer.language.equalsIgnoreCase("java")){
					fileName = fileName + ".java"
				} else {
					fileName = fileName + ".groovy"
				}
				
				log.info 'Writing service file to: ' + fileName
				
				new File(fileName.substring(0, fileName.lastIndexOf("\\"))).mkdirs()
				
				new File(fileName).withWriter('utf-8') { writer ->
					writer.writeLine content
				}			
				

				
					
				content = downloadService(offer.name, offer.output.type)

				packagePathElements = offer.output.type.split("\\.");
				fichier = outputFolder + File.separator + "src" + File.separator + "main" + File.separator + "orcha" + File.separator
				for(String element: packagePathElements){
					fichier = fichier + element + File.separator
				}
					
				fileName = fichier.substring(0, fichier.size()-1)
				
				if(offer.language.equalsIgnoreCase("java")){
					fileName = fileName + ".java"
				} else {
					fileName = fileName + ".groovy"
				}
				
				log.info 'Writing service file to: ' + fileName
				
				new File(fileName.substring(0, fileName.lastIndexOf("\\"))).mkdirs()
				
				new File(fileName).withWriter('utf-8') { writer ->
					writer.writeLine content
				}

				
				
				
				if(offer.input.adapter instanceof orcha.lang.configuration.JavaServiceAdapter){
					
					content = downloadService(offer.name, offer.input.adapter.javaClass)
					
					packagePathElements = offer.input.adapter.javaClass.split("\\.");
					fichier = outputFolder + File.separator + "src" + File.separator + "main" + File.separator + "orcha" + File.separator
					for(String element: packagePathElements){
						fichier = fichier + element + File.separator
					}
						
					fileName = fichier.substring(0, fichier.size()-1)
					
					if(offer.language.equalsIgnoreCase("java")){
						fileName = fileName + ".java"
					} else {
						fileName = fileName + ".groovy"
					}
					
					log.info 'Writing service file to: ' + fileName
					
					new File(fileName.substring(0, fileName.lastIndexOf("\\"))).mkdirs()
					
					new File(fileName).withWriter('utf-8') { writer ->
						writer.writeLine content
					}
					
				} else if(offer.input.adapter instanceof orcha.lang.configuration.ScriptServiceAdapter){
					
					content = downloadService(offer.name, offer.input.adapter.file)
					
					if(offer.input.adapter.file.startsWith("file:")){
						fileName = offer.input.adapter.file.substring(5)
					} else {
						fileName = offer.input.adapter.file
					}
					
					
					
					fileName = fileName.replace('/', '\\')
					
					packagePathElements = fileName.split("\\\\");
					fichier = outputFolder + File.separator
					for(String element: packagePathElements){
						fichier = fichier + element + File.separator
					}
						
					fileName = fichier.substring(0, fichier.size()-1)
					
					/*if(offer.language.equalsIgnoreCase("java")){
						fileName = fileName + ".java"
					} else {
						fileName = fileName + ".groovy"
					}*/
					
					log.info 'Writing service file to: ' + fileName
	
					new File(fileName.substring(0, fileName.lastIndexOf("\\"))).mkdirs()
					
					new File(fileName).withWriter('utf-8') { writer ->
						writer.writeLine content
					}
				}
			}
		}
		
			
		
		log.info 'Missing Orcha configuration details for the compilation => service selector generation complete successfully'
		
		return true
		
	
	}
	
	private String downloadService(String applicationName, String service){
				
		String uri = registryURI + "services/" + applicationName + "?serviceName=" + service
		
		log.info "Download service: " + service + " at: " + uri
		
		def http = new HTTPBuilder(uri)
				
		String content = ""
		
		http.request(groovyx.net.http.Method.GET,ContentType.TEXT) { req ->
			response.success = { resp, inputStreamReader  ->

				char[] buf = new char[1024]
				int nbChar
				while((nbChar=inputStreamReader.read(buf, 0, 1024)) != -1){
					content = content + String.copyValueOf(buf, 0, nbChar)
				}
								   
			}
			response.failure = { resp ->
				throw new Exception("Error: ${resp.status}")
			}
		}
		
		log.info "Download service completed successfully: " + service + " at: " + uri
		
		return content

	}

}
