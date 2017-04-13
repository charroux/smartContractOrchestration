package orcha.lang.compiler

import java.util.List;
import java.util.Map;

import groovy.xml.XmlUtil
import orcha.lang.business.BusinessAdapter
import orcha.lang.business.BusinessConfiguration;
import orcha.lang.compiler.visitor.OrchaCodeParser

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component

@Component
class CommandLineRunnerCompiler implements CommandLineRunner{
	
	@Autowired
	BusinessAdapter businessAdapter
	
	@Autowired
	Compile compile
	
	@Autowired
	OrchaCodeParser composeCodeParser

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
		
		compile.compile(composeCodeParser)
				
	}

}
