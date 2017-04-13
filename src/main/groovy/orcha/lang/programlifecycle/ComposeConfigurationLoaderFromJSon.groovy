package orcha.lang.programlifecycle

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j;

import orcha.lang.configuration.Application
import orcha.lang.configuration.DatabaseAdapter
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.OutputFileAdapter
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext

import java.nio.file.Files
import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * When generate a compose configuration file from a JSon compose configuration 
 * the generated beans are not loaded at program launching despite a complete rebuild of the project.
 * 
 * So this class loads bean dynamically from json configuration.
 * 
 * @author Charroux_std
 *
 */
@Slf4j
class ComposeConfigurationLoaderFromJSon {
	
	@Autowired
	ApplicationContext context
	
	/**
	 * 
	 * @param json
	 * @param generateGroovySourceFile
	 * @param groovyClassName null if generateGroovySourceFile is false
	 * @return
	 */
	def loadBeanFromJSon(String json){
		
		def jsonObject = new JsonSlurper().parseText(json)
		
		if(jsonObject instanceof ArrayList == false){
			throw new Exception("The program configuration should be an array of configuration")
		}
		
		def applications = []
		
		jsonObject.each{
		
			if(it.input!=null && it.input.adapter.adapter == "JavaApplication"){
				Application application = new Application(it)
				application.input.adapter = new JavaServiceAdapter(it.input.adapter)
				application.output.adapter = new JavaServiceAdapter(it.output.adapter)
				applications.add(application)
			} else if(it.input!=null && it.input.adapter.adapter == "File"){
				EventHandler eventHandler = new EventHandler(it)
				eventHandler.input.adapter = new InputFileAdapter(it.input.adapter)
				eventHandler.output = null
				applications.add(eventHandler)
			} else if(it.output!=null && it.output.adapter.adapter == "File"){
				EventHandler eventHandler = new EventHandler(it)
				eventHandler.output.adapter = new OutputFileAdapter(it.output.adapter)
				eventHandler.input = null
				applications.add(eventHandler)
			}  else if(it.output!=null && it.output.adapter.adapter == "SQL"){
				EventHandler eventHandler = new EventHandler(it)
				eventHandler.output.adapter = new DatabaseAdapter(it.output.adapter)
				eventHandler.input = null
				applications.add(eventHandler)
			}
			
			
		}
		
		def applicationsWithoutBean = []
			
		applications.each{
				
			try{
				
				def applicationAsBean = context.getBean(it.name)
					
				log.info "Bean " + it.name + " already exists"
						
			}catch(NoSuchBeanDefinitionException e){
				applicationsWithoutBean.add(it)
			}
					
		}
			
		if(applicationsWithoutBean.size() > 0){

			/*if(generateGroovySourceFile == true){
				
				JavaCodeGenerator javaCodeGenerator = new JavaCodeGenerator()
				javaCodeGenerator.setContext(context)
						
				javaCodeGenerator.generateConfiguration(applicationsWithoutBean, groovyClassName)
						
				// rename the file *.java in *.groovy because the java file can not find the groovy classes ! Why ?
						
				String s = "." + File.separator + "compose" + File.separator + "configuration" + File.separator + groovyClassName + ".java"
				File oldFile = new File(s)
				s = "." + File.separator + "compose" + File.separator + "configuration" + File.separator + groovyClassName + ".groovy"
				File newFile = new File(s);
				if(newFile.exists()){
					newFile.delete()
				}
				Files.move(oldFile.toPath(), newFile.toPath());
				
			}*/
			
			ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
			
			applicationsWithoutBean.each{
						
				beanFactory.registerSingleton(it.name, it);
						
				log.info "New bean " + it.name + " added"
				
			}
	
		}

	}

}
