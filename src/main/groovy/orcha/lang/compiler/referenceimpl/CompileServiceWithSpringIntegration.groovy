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
import orcha.lang.compiler.visitor.OrchaCodeParser

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
	
	/**
	 * @param orchaCodeParser
	 * @param commandLineArgs
	 * @throws OrchaCompilationException
	 * @throws OrchaConfigurationException
	 */
	@Override
	public void compile(OrchaCodeParser orchaCodeParser) throws OrchaCompilationException, OrchaConfigurationException {
		
		log.info 'Transpilatation of the Orcha program begins'
		
		String xmlSpringContextFileName = orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"		
		String xmlSpringContent = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + xmlSpringContextFileName 
		
		//String xmlQoSSpringContent = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "orchaQoSSpringContext.xml"
		String xmlSpringContextQoSFileName = orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
		String xmlQoSSpringContent = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + xmlSpringContextQoSFileName 
				
		xmlGenerator.generate(orchaCodeParser, xmlSpringContent, xmlQoSSpringContent)
				
		String xmlContext = new File(xmlSpringContent).text
		String springContexteAsText = XmlUtil.serialize(xmlContext)
		new File(xmlSpringContent).withWriter('utf-8') { writer ->
			writer.writeLine springContexteAsText
		}
		
		File oldFile = new File(xmlSpringContent)
		
		// used when the an executable jar is built
		//def xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + "orchaSpringContext.xml"
		def xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + ".xml"
		File newFile = new File(xmlSpringContentInSrc);
		FileOutputStream fos = new FileOutputStream(newFile)
				
		Files.copy(oldFile.toPath(), fos);
		fos.close()
		
		log.info 'Transpilatation complete successfully. Orcha orchestrator generated into ' + xmlSpringContentInSrc
		
		xmlContext = new File(xmlQoSSpringContent).text
		springContexteAsText = XmlUtil.serialize(xmlContext)
		new File(xmlQoSSpringContent).withWriter('utf-8') { writer ->
			writer.writeLine springContexteAsText
		}
		
		oldFile = new File(xmlQoSSpringContent)
		
		// used when the an executable jar is built
		// xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + "orchaQoSSpringContext.xml"
		xmlSpringContentInSrc = "." + File.separator + "bin" + File.separator + orchaCodeParser.getOrchaMetadata().getTitle() + "QoS.xml"
		newFile = new File(xmlSpringContentInSrc);
		fos = new FileOutputStream(newFile)
				
		Files.copy(oldFile.toPath(), fos);
		fos.close()

		log.info 'Transpilatation complete successfully. QoS for Orcha orchestrator generated into ' + xmlSpringContentInSrc
		
		orchaLauncherGenerator.generate(xmlSpringContextFileName, xmlSpringContextQoSFileName)
		
	}

/*	@Override
	public int getExitCode() {
		// TODO Auto-generated method stub
		return 10;
	}*/
	
}
