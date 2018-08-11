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
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.XmlGeneratorForSpringIntegration
import orcha.lang.compiler.visitor.OrchaCodeVisitor

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@ComponentScan(basePackages=['orcha.lang.compiler.referenceimpl.xmlgenerator'])
class CompileServiceWithSpringIntegration implements Compile{
		
	@Autowired
	ApplicationContext context
	
	@Autowired
	XmlGenerator xmlGenerator
	
	@Autowired
	OrchaLauncherGenerator orchaLauncherGenerator

	@Override
	public void compileForLaunching(OrchaCodeVisitor orchaCodeParser) throws OrchaCompilationException, OrchaConfigurationException {
		
		String path = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator
		log.info 'Transpilatation of the orcha program \"' + orchaCodeParser.getOrchaMetadata().getTitle() + '\" into the directory ' + path
		
		this.compile(orchaCodeParser, new File(path))
		
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

			String path = "." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
			log.info 'Transpilatation of the orcha testing program \"' + orchaCodeParser.getOrchaMetadata().getTitle() + '\" into the directory ' + path
			
			this.compile(orchaCodeParser, new File(path));
			
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
	public void compile(OrchaCodeVisitor orchaCodeParser, File destinationDirectory) throws OrchaCompilationException, OrchaConfigurationException {
		
		log.info 'Transpilatation of the Orcha program begins'
		
		String xmlSpringContextFileName = orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"		
		String xmlSpringContent = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextFileName
		
		String xmlSpringContextQoSFileName = orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
		String xmlQoSSpringContent = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextQoSFileName
				
		xmlGenerator.generate(orchaCodeParser, new File(xmlSpringContent), new File(xmlQoSSpringContent))

		File oldFile = new File(xmlSpringContent)
		
		// used when the an executable jar is built
		def xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"
		File newFile = new File(xmlSpringContentInSrc);
		FileOutputStream fos = new FileOutputStream(newFile)
		Files.copy(oldFile.toPath(), fos);
		fos.close()

		log.info 'Transpilatation complete successfully. Orcha orchestrator copied into ' + xmlSpringContentInSrc
		
		// Update temporary file 
		xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + "main" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"
		newFile = new File(xmlSpringContentInSrc)
		fos = new FileOutputStream(newFile)
		Files.copy(oldFile.toPath(), fos);
		fos.close()
		
		log.info 'Transpilatation complete successfully. Orcha orchestrator copied into ' + xmlSpringContentInSrc
		
		oldFile = new File(xmlQoSSpringContent)
				
		// used when the an executable jar is built
		xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
		newFile = new File(xmlSpringContentInSrc);
		fos = new FileOutputStream(newFile)				
		Files.copy(oldFile.toPath(), fos);
		fos.close()

		log.info 'Transpilatation complete successfully. QoS for Orcha orchestrator copied into ' + xmlSpringContentInSrc
		
		// update temporary file
		xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + "main" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
		newFile = new File(xmlSpringContentInSrc);
		fos = new FileOutputStream(newFile)
		Files.copy(oldFile.toPath(), fos);
		fos.close()

		log.info 'Transpilatation complete successfully. QoS for Orcha orchestrator copied into ' + xmlSpringContentInSrc

	}

	
}
