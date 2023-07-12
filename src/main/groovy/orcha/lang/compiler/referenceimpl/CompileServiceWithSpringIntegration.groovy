package orcha.lang.compiler.referenceimpl

import groovy.util.logging.Slf4j;
import groovy.xml.XmlUtil
import orcha.lang.compiler.Compile;
import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.referenceimpl.configurationproperties.ConfigurationPropertiesGenerator
import orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator
import orcha.lang.compiler.referenceimpl.xmlgenerator.XmlGenerator
import orcha.lang.compiler.referenceimpl.xmlgenerator.connectors.SpringIntegrationConnectors
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ErrorUnwrapper;
import orcha.lang.compiler.visitor.OrchaCodeVisitor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.List;
import java.util.Map;

@Slf4j
//@Configuration
@ComponentScan(basePackages=['orcha.lang.compiler', 'orcha.lang.compiler.referenceimpl.xmlgenerator'])
class CompileServiceWithSpringIntegration implements Compile{

	@Value('${orcha.pathToBinaryCode:build/resources/main}')
	String pathToBinaryCode;

	@Autowired
	ApplicationContext context
	
	@Autowired
	XmlGenerator xmlGenerator
	
	@Autowired
	OrchaLauncherGenerator orchaLauncherGenerator
	
	@Autowired
	ConfigurationPropertiesGenerator configurationPropertiesGenerator
	
	@Override
	public void compileForLaunching(OrchaCodeVisitor orchaCodeParser) throws OrchaCompilationException, OrchaConfigurationException {
		
		String pathToSouresCode = "." + File.separator + "src" + File.separator + "main"
		log.info 'Transpilatation of the orcha program \"' + orchaCodeParser.getOrchaMetadata().getTitle() + '\" into the directory ' + pathToSouresCode + File.separator + "resources"

		String pathToBinCode = "."

		def directories = pathToBinaryCode.split("/")
		directories.each {
			pathToBinCode = pathToBinCode + File.separator + it
		}

		//String pathToBinCode = "." + File.separator + pathToBinaryCode + File.separator + "main"
		log.info 'Path to binary generated code ' + pathToBinCode
		
		this.compile(orchaCodeParser, new File(pathToSouresCode), new File(pathToBinCode))
		
		String xmlSpringContextFileName = orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"
		String xmlSpringContextQoSFileName = orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
		
		orchaLauncherGenerator.generateForLaunching(xmlSpringContextFileName, xmlSpringContextQoSFileName)
	}

	/**
	 * @param orchaCodeParser or null if there is no testing file but an empty test launcher should be generated
	 * @throws OrchaCompilationException
	 * @throws OrchaConfigurationException
	 */

	@Override
	public void compileForTesting(OrchaCodeVisitor orchaCodeParser) throws OrchaCompilationException, OrchaConfigurationException {
		
		if(orchaCodeParser != null){

			String pathToSouresCode = "." + File.separator + "src" + File.separator + "test"
			log.info 'Transpilatation of the orcha testing program \"' + orchaCodeParser.getOrchaMetadata().getTitle() + '\" into the directory ' + pathToSouresCode  + File.separator + "resources"

//			String pathToBinCode = "." + File.separator + pathToBinaryCode + File.separator + "test"
			String pathToBinCode = "."

			def directories = pathToBinaryCode.split("/")
			directories.each {
				pathToBinCode = pathToBinCode + File.separator + it
			}

			log.info 'Path to binary generated code ' + pathToBinCode
			
			this.compile(orchaCodeParser, new File(pathToSouresCode), new File(pathToBinCode))
			
			String xmlSpringContextFileName = orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"
			String xmlSpringContextQoSFileName = orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
			
			orchaLauncherGenerator.generateForTesting(xmlSpringContextFileName, xmlSpringContextQoSFileName)
	
		} else {
			
			orchaLauncherGenerator.generateForTesting(null, null)
			
		}
	}

	/**
	 * @param orchaCodeParser
	 * @throws OrchaCompilationException
	 * @throws OrchaConfigurationException
	 */
	@Override
	public void compile(OrchaCodeVisitor orchaCodeParser, File sourceCodeDirectory, File binaryCodeDirectory) throws OrchaCompilationException, OrchaConfigurationException {
		
		log.info 'Transpilatation of the Orcha program begins'
		
		//configurationPropertiesGenerator.resetSpringCloudStream(sourceCodeDirectory, binaryCodeDirectory)

		//log.info 'Transpilatation of the Orcha program begins'

		xmlGenerator.generate(orchaCodeParser, sourceCodeDirectory, binaryCodeDirectory)

		log.info 'Transpilatation complete successfully'
		
	}
	
}
