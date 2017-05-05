package orcha.lang

import orcha.lang.business.BusinessAdapter
import orcha.lang.business.BasicBusinessAdapter
import orcha.lang.compiler.Compile;
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.compiler.referenceimpl.CompileServiceWithSpringIntegration
import orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ErrorUnwrapper;
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.impl.OrchaCodeVisitor

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ImportResource;

import groovy.util.logging.Slf4j

@Slf4j
@Configuration
@ComponentScan(basePackages=['orcha.lang.compiler','orcha.lang.business','configuration'])
class Compiler implements CommandLineRunner{
	
	/*@Bean
	Compile compile(){
		return new CompileServiceWithSpringIntegration()
	}
	
	@Bean
	OrchaCodeParser composeCodeParser(){
		return new OrchaCodeVisitor()
	} 
	
	@Bean
	BusinessAdapter businessAdapter(){
		return new BasicBusinessAdapter()
	}
	
	@Bean
	QualityOfService qualityOfService(){
		return new QualityOfServiceImpl()
	}*/
	
	@Autowired
	BusinessAdapter businessAdapter
	
	@Autowired
	Compile compile
	
	@Autowired
	OrchaCodeParser composeCodeParser
	
	@Autowired
	ConfigurationMockGenerator configurationMockGenerator

	@Override
	public void run(String... args) throws Exception {
		
		if(args.length != 1){
			throw new OrchaConfigurationException("Usage: orchaSourceFile.\nAn orcha source file (.orcha or .groovy extension) should be in ./orcha/source\nPut the file name without the extension (.orcha or .groovy) as the argument of this command.\nIf the orcha file is in a subdirectory of ./orcha/source, add this subdirectory to the command line like directoryName/orchaFileNameWithOutExtension")
		}
		
		String orchaFile = args[0]
		
		if(orchaFile.endsWith(".orcha")){
			orchaFile = businessAdapter.adaptOrchaFileToBusiness(orchaFile)
		}
		
		if(orchaFile.endsWith(".groovy") == false){
			throw new OrchaConfigurationException("An orcha source file (.orcha or .groovy extension) should be in ./orcha/source\nPut the file name without the extension (.orcha or .groovy) as the argument of this command.\nIf the orcha file is in a subdirectory of ./orcha/source, add this subdirectory to the command line like directoryNama/orchaFileNameWithOutExtension")
		}
				
		composeCodeParser.parseSourceFile(orchaFile)
		
		boolean isMockGenerated = configurationMockGenerator.generate(composeCodeParser)
		
		if(isMockGenerated == false){
			compile.compile(composeCodeParser)
		}
		
	}
	
	public static void main(String[] args) {
		
		SpringApplication application = new SpringApplication(Compiler.class)
		application.setWebEnvironment(false)
		ConfigurableApplicationContext configurableApplicationContext = application.run(args)
		
		ConfigurationMockGenerator configurationMockGenerator = configurableApplicationContext.getBean("configurationMockGenerator")
		
		if(configurationMockGenerator.getIsMockGenerated()){
			
			log.info "Fix uncomplete Orcha configuration: trying to compile again the Orcha program..."

			configurableApplicationContext.close()
			
			application = new SpringApplication(Compiler.class)
			application.setWebEnvironment(false)
			application.run(args)
			
			log.info "Compilation of the Orcha program successful."
	
		}
	
	}

}
