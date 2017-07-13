package orcha.lang

import java.io.File

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

import groovy.util.logging.Slf4j
import orcha.lang.business.BusinessAdapter
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.contract.ContractGenerator

@Slf4j
@Configuration
@ComponentScan(basePackages=['orcha.lang.compiler','orcha.lang.contract','orcha.lang.business','configuration','orcha.lang.registry'])
class ContractGeneration  implements CommandLineRunner{

	@Autowired
	BusinessAdapter businessAdapter
	
	@Autowired
	OrchaCodeParser orchaCodeParser
	
	@Autowired
	ContractGenerator contractGenerator
	
	@Override
	public void run(String... args) throws Exception {
		
		log.info "Contract generation begins.."
		
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
		
		contractGenerator.generateAll(orchaCodeVisitor)
		
		String outputXMLPath = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "contract1.xml"
		File outputXMLFile = new File(outputXMLPath)
		contractGenerator.exportToXML(outputXMLFile)
		
		log.info "Contract generation complete successfully into: " + outputXMLFile.getAbsolutePath()
		
	}
	
	public static void main(String[] args) {
		
		SpringApplication application = new SpringApplication(ContractGeneration.class)
		application.setWebEnvironment(false)
		application.run(args)
			
	}

}
