package orcha.lang

import orcha.lang.business.BusinessAdapter
import orcha.lang.business.BasicBusinessAdapter
import orcha.lang.compiler.Compile;
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.compiler.referenceimpl.CompileServiceWithSpringIntegration
import orcha.lang.compiler.referenceimpl.OrchaLauncherGenerator
import orcha.lang.compiler.referenceimpl.configurationproperties.ConfigurationPropertiesGenerator
import orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ApplicationsListToObjectsListTransformer
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ErrorUnwrapper;
import orcha.lang.compiler.serviceOffer.ServiceOfferSelectionGenerator
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.compiler.visitor.impl.OrchaCodeVisitorImpl

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
@ComponentScan(basePackages=['orcha.lang.compiler','orcha.lang.business','configuration','orcha.lang.registry'])
class Compiler implements CommandLineRunner{
	
	@Autowired
	BusinessAdapter businessAdapter
	
	@Autowired
	Compile compile
	
	@Autowired
	OrchaCodeParser orchaCodeParser
	
	@Autowired
	OrchaCodeParser orchaTestCodeParser
	
	@Autowired
	ConfigurationMockGenerator configurationMockGenerator
	
	@Autowired
	ConfigurationPropertiesGenerator configurationPropertiesGenerator

	@Autowired
	ServiceOfferSelectionGenerator serviceOfferSelectionGenerator
	
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
				
		String pathToCode = "." + File.separator + "src" + File.separator + "main" + File.separator + "orcha" + File.separator + "source" + File.separator + orchaFile
		File orchaSourceFile = new File(pathToCode)
				
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaSourceFile)
		
		String[] instructions = orchaCodeVisitor.toStringGraphOfInstructions()
		for(String s: instructions){
			println s
		}
		
		serviceOfferSelectionGenerator.generate(orchaCodeVisitor)
		
		boolean isMockGenerated = configurationMockGenerator.generate(orchaCodeVisitor)
		
		if(isMockGenerated == false){
			
			compile.compileForLaunching(orchaCodeVisitor)
			
			configurationPropertiesGenerator.generate(orchaCodeVisitor)
			
			String testFolder = orchaSourceFile.getParent().replace("main", "test")
			File orchaTestSourceDirectory = new File(testFolder)
			File[] testFiles = orchaTestSourceDirectory.listFiles()
			if(testFiles!=null && testFiles.length > 0){
				OrchaCodeVisitor orchaTestCodeVisitor = orchaTestCodeParser.parse(testFiles[0])
				compile.compileForTesting(orchaTestCodeVisitor)
			}
			
		}
		
	}
	
	public static void main(String[] args) {

		SpringApplication application = new SpringApplication(Compiler.class)
		application.setWebEnvironment(false)
		ConfigurableApplicationContext configurableApplicationContext = application.run(args)
		
		ConfigurationMockGenerator configurationMockGenerator = configurableApplicationContext.getBean("configurationMockGenerator")
		
		if(configurationMockGenerator.getIsMockGenerated()){
			
			log.info "Uncomplete Orcha configuration fixed by an auto generated configuration. Trying now to compile again the Orcha program..."

			configurableApplicationContext.close()
			
			application = new SpringApplication(Compiler.class)
			application.setWebEnvironment(false)
			application.run(args)
			
		}
		
		log.info "Compilation of the Orcha program successful. The Orcha program (orcha.lang.OrchaSpringIntegrationLauncher.groovy) can be launched."		
	
	}

}
