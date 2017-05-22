package orcha.lang.compiler.serviceOffer

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
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.filter.AnnotationTypeFilter

@Slf4j
class ServiceOfferSelectionGenerator {
	
	String registryURI = "http://localhost:8080/applications"
	
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
		
		log.info 'Missing Orcha configuration details for the compilation => auto generate a service selector...'
		
		def offersByapplication = [:]
		
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
					
					String uri = registryURI + "?input=" + inputType + "&output=" + outputType
			
					def http = new HTTPBuilder(uri)
					
					http.request(groovyx.net.http.Method.GET,ContentType.JSON) { req ->
						response.success = { resp, apps  ->
							apps.each{ appli ->				
								
								def input = new Input(appli.input)
								
								def adapter
								
								if(appli.language.equalsIgnoreCase("java") || appli.language.equalsIgnoreCase("groovy")){
									adapter = new JavaServiceAdapter(appli.output.adapter)
								} else if(appli.language.equalsIgnoreCase("js") || appli.language.equalsIgnoreCase("javascript") || appli.language.equalsIgnoreCase("java script")){
									adapter = new ScriptServiceAdapter(appli.output.adapter)
								}
								
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
		
		offersByapplication.each { application, offers ->
			
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
				
				
				String packageName = "package source." + application.name
				writer.writeLine packageName
								
				String comparison = "compute select" + className + " with "
				
				String when = "when \"select" + className + " terminates\""
				
				send = "send select" + className + ".result to " + send + "Output"
				
				className = className.concat("OffersComparison")
				
				String synchronize = "when \"("
				
				int i = 0
				
				offers.each{ offer ->
					writer.writeLine "receive " + event + " from " + event + "EventHandler"
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
						
		}
		
		log.info 'Missing Orcha configuration details for the compilation => service selector generation complete successfully'
		
		return true
		
	}

}
