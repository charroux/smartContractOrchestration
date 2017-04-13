package orcha.lang.programlifecycle

import orcha.lang.compiler.Instruction
import orcha.lang.compiler.Instruction.With
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.Input
import orcha.lang.configuration.InputFileAdapter
import orcha.lang.configuration.Output
import orcha.lang.configuration.OutputFileAdapter
import org.codehaus.groovy.control.CompilationUnit

import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import org.springframework.core.env.AbstractEnvironment

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j;

import java.nio.file.Files
import java.nio.file.FileSystems
import java.nio.file.Path

@Component
@Slf4j
class BuildAllAndRun {
	
	OrchaProgram composeProgram
	
	//Application application
	
	@Value('${composeSourceDirectory}')
	private String composeSourceDirectory;
	
	@Value('${composeConfigurationDirectory}')
	private String composeConfigurationDirectory;
	
	@Autowired
	ApplicationContext context
	
	@Autowired
	ComposeConfigurationLoaderFromJSon composeConfigurationLoaderFromJSon
	
	ConfigurableApplicationContext configurableApplicationContext
	
	def interactiveModeDecision(){
		
		log.info "interactiveModeDecision"
		
		if(composeProgram == null){
			throw new Exception("composeProgram should not be null")
		}
		
		if(composeProgram.interactiveMode == false){
			throw new Exception("Batch mode")
		}
	}
	
	def updateComposeProgram(){
		
		if(composeProgram == null){
			throw new Exception("composeProgram should not be null")
		}	
			
		if(composeProgram.programName == null){
			throw new Exception("composeProgram has no program name")
		}
			
		log.info "UpdateComposeProgram"
		
		boolean newComposeProgramNeeded = false
		
		Path path = FileSystems.getDefault().getPath(composeSourceDirectory, composeProgram.programName)
		
		try{
			
			byte[] oldComposeProgram = Files.readAllBytes(path)
			
			// delete and replace character 13 ('\r') by 10 ('\n')
			oldComposeProgram = Arrays.copyOf(oldComposeProgram, oldComposeProgram.length-1)
			oldComposeProgram[oldComposeProgram.length-1] = 10
			
			byte[] newComposeProgram = composeProgram.program.bytes
			
			if(Arrays.equals(oldComposeProgram, newComposeProgram) == false){
				newComposeProgramNeeded = true
			}
			
		}catch(java.nio.file.NoSuchFileException e){
			newComposeProgramNeeded = true
		}
		
		if(newComposeProgramNeeded == true){
			new File(composeSourceDirectory + composeProgram.programName).withWriter('utf-8') { writer ->
				writer.writeLine composeProgram.program.trim()
			}
			log.info "Compose program updated: " + composeSourceDirectory + composeProgram.programName
		} else {
			log.info "Compose program already exits: " + composeSourceDirectory + composeProgram.programName
		}
	}

	def updateInteractiveComposeProgram(){
		
		if(composeProgram == null){
			throw new Exception("composeProgram should not be null")
		}
		
		composeProgram.program = composeProgram.program.trim()
		
		if(composeProgram.program.indexOf("\n") != -1){
			throw new Exception("An interactive program should have only one line")
		}
		
		int index = composeProgram.program.indexOf(" ")
		if(index == -1){
			throw new Exception("Syntax error on program")
		}
		
		Instruction instruction = new Instruction()
		
		instruction.instruction = composeProgram.program.substring(0, index)	// receive
		
		instruction.variable = composeProgram.program.substring(index).trim()
		index = instruction.variable.indexOf(" ")
		if(index == -1){
			throw new Exception("Syntax error on program")
		}
		
		instruction.springBean = instruction.variable.substring(index).trim()
			
		instruction.variable = instruction.variable.substring(0, index)		// event
		
		if(instruction.instruction=="receive" && instruction.springBean.startsWith("from ")==false){
			throw new Exception("Syntax error on program: from is missing")
		}
		
		if(instruction.instruction=="compute" && instruction.springBean.startsWith("with ")==false){
			throw new Exception("Syntax error on program: with is missing")
		}
		
		index = instruction.springBean.indexOf(" ")
		if(index == -1){
			throw new Exception("Syntax error on program")
		}
		
		instruction.springBean = instruction.springBean.substring(index).trim()
		
		if(instruction.instruction == "receive"){
			
			String addedInstruction = "send " + instruction.variable + ".value to " + instruction.variable + "File" 
			
			composeProgram.program = composeProgram.program + "\n" + addedInstruction
			  
			log.info "program: " + composeProgram.program
			
			EventHandler eventHandler = new EventHandler()
			eventHandler.name = instruction.variable + "File" 
			Output output = new Output()
			output.mimeType = "text/plain"
			output.type = "java.lang.String"
			eventHandler.output = output
			OutputFileAdapter outputFileAdapter = new OutputFileAdapter()
			outputFileAdapter.appendNewLine = true
			outputFileAdapter.createDirectory = true
			outputFileAdapter.directory = "C:/Users/Charroux_std/Documents/projet/ExecAndShare/Compose/ComposeTools/output"
			outputFileAdapter.writingMode = "REPLACE"
			outputFileAdapter.filename = instruction.variable + "File.txt"
			output.adapter = outputFileAdapter
			
			String addedConfiguration = JsonOutput.toJson(eventHandler)
			
			composeProgram.configuration = "[" + composeProgram.configuration + ",\n" + addedConfiguration + "]"
			
			log.info "config: " + composeProgram.configuration
			
		} else if(instruction.instruction == "compute"){
		
			def withs = []
			
			index = instruction.springBean.indexOf(".")
			if(index == -1){
				throw new Exception("Syntax error on program")
			}
			
			def with = instruction.springBean.substring(0, index)	// with
			def withProperty = instruction.springBean.substring(index+1).trim()
			
			With w = new With()
			w.with = with
			w.withProperty = withProperty
						
			withs.add(w)
			 
			instruction.setWiths(withs)
		
			String addedInstruction = "receive " + instruction.withs[0].with + " from " + instruction.withs[0].with + "File"
			
			composeProgram.program = addedInstruction + "\n" + composeProgram.program 
			
			addedInstruction = "when \"" + instruction.variable + " terminates\""
			
			composeProgram.program = composeProgram.program + "\n" + addedInstruction
			
			addedInstruction = "send " + instruction.variable + ".result to " + instruction.variable + "File"
			
			composeProgram.program = composeProgram.program + "\n" + addedInstruction
			
			log.info "program: " + composeProgram.program
			
			EventHandler eventHandler = new EventHandler()
			eventHandler.name = instruction.withs[0].with + "File"
			Input input = new Input()
			input.mimeType = "text/plain"
			input.type = "java.lang.String"
			eventHandler.input = input
			InputFileAdapter inputFileAdapter = new InputFileAdapter()
			inputFileAdapter.directory = "C:/Users/Charroux_std/Documents/projet/ExecAndShare/Compose/ComposeTools/input"
			inputFileAdapter.filenamePattern = "*.txt"
			input.adapter = inputFileAdapter
			
			String addedConfiguration = JsonOutput.toJson(eventHandler)
			
			composeProgram.configuration = "[" + addedConfiguration + ",\n" + composeProgram.configuration
			
			eventHandler = new EventHandler()
			eventHandler.name = instruction.variable + "File" 
			Output output = new Output()
			output.mimeType = "text/plain"
			output.type = "java.lang.String"
			eventHandler.output = output
			OutputFileAdapter outputFileAdapter = new OutputFileAdapter()
			outputFileAdapter.appendNewLine = true
			outputFileAdapter.createDirectory = true
			outputFileAdapter.directory = "C:/Users/Charroux_std/Documents/projet/ExecAndShare/Compose/ComposeTools/output"
			outputFileAdapter.writingMode = "REPLACE"
			outputFileAdapter.filename = instruction.variable + "File.txt"
			output.adapter = outputFileAdapter
			
			addedConfiguration = JsonOutput.toJson(eventHandler)
			
			composeProgram.configuration = composeProgram.configuration + ",\n" + addedConfiguration + "]"
			
			log.info "config: " + composeProgram.configuration
		}
		 
	}
	
	def configureTheComposeProgram(){
		
		String configurationClassName = composeProgram.programName
		int index = configurationClassName.lastIndexOf('.')
		configurationClassName = configurationClassName.substring(0, index).concat("Configuration")	//.concat(".groovy")
		
		composeConfigurationLoaderFromJSon.loadBeanFromJSon(composeProgram.configuration)
		
		Path path = FileSystems.getDefault().getPath(composeConfigurationDirectory, configurationClassName + '.json')
		
		BufferedWriter bw = Files.newBufferedWriter(path)
		bw.write(composeProgram.configuration)
		bw.close()
		
		log.info "Json file with configuration: " + path.toString()
		
	}
	
	def compileComposeProgram(){
		
		String[] args = [composeProgram.programName, "false"]
		
		//def compileService = new CompileService(context: context)
		compileService.compile(args)
		
	}
	
	def generateExecutableDecision(){
		
		log.info "generateExecutableDecision"
		
		if(composeProgram == null){
			throw new Exception("composeProgram should not be null")
		}
		
		if(composeProgram.generateExecutable == false){
			throw new Exception("No executable generation")
		}
	}
	
	def buildProject(){
		
		def gradleClean = 'gradle.bat clean'.execute()
		gradleClean.waitFor()
		
		String gradleOutput  = gradleClean.getText()
		log.info "cleanProject: " + gradleOutput
		
		def gradleBuild = 'gradle.bat build'.execute()
		gradleBuild.waitFor()
		
		gradleOutput  = gradleBuild.getText()
		log.info "buildProject: " + gradleOutput
		
	}
	
	def launchProgram(){
		
		System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "default");	// restoring the String http default port to 8080 (application.properties)
		SpringApplication application = new SpringApplication(org.olabdynamics.compose.main.Main.class)
		String[] args = new String[0]
		configurableApplicationContext = application.run(args)

	}
	
	def stopProgram(){
		
		//println "isactive: " + configurableApplicationContext.isActive()
		configurableApplicationContext.close();
		//println "isactive: " + configurableApplicationContext.isActive()
	}
	
	boolean isRunning(){
		if(configurableApplicationContext != null){
			return configurableApplicationContext.isActive()
		} else {
			return false
		}		
	}

}
