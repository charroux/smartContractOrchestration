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
			String outputFolder = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources"
			
			ZipInputStream zis =  new ZipInputStream(new FileInputStream(projectTemplateFile))
			
			ZipEntry ze = zis.getNextEntry()

			byte[] buffer = new byte[1024]
			
			while(ze!=null){
				
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);
			
				System.out.println("file unzip : "+ newFile.getAbsoluteFile());
				//create all non exists folders
				//else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();
			
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
				   fos.write(buffer, 0, len);
				}
			
				fos.close();
				ze = zis.getNextEntry();
			}
			
			zis.closeEntry();
			zis.close();
		}
		
		return true
		
	}

}
