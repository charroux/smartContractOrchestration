package orcha.lang.compiler.referenceimpl.javacodegeneration

import groovy.util.logging.Slf4j;

import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.HttpAdapter
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean

import com.sun.codemodel.JBlock
import com.sun.codemodel.JCodeModel
import com.sun.codemodel.JDefinedClass
import com.sun.codemodel.ClassType
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar
import com.sun.codemodel.JInvocation
import com.sun.codemodel.JMethod
import com.sun.codemodel.JMod
import com.sun.codemodel.JVar
import com.sun.codemodel.JExpr
import com.sun.codemodel.writer.FileCodeWriter

@Slf4j
class JavaCodeGenerator {
	
	ApplicationContext context
	def instructions
	
	def generate(){
		
		log.info "generate Java Code"
		
		String s = "." + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "generated"
		File outputDirectory = new File(s)
		File[] files = outputDirectory.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				if(f.isDirectory()==false){
					f.delete();
				}
			}
		}
		
		log.info "empty " + s 
		
		s = "." + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "myservice"
		outputDirectory = new File(s)
		files = outputDirectory.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				if(f.isDirectory()==false){
					f.delete();
				}
			}
		}
		
		log.info "empty " + s 
		
		instructions.each{
					
			if(it.instruction == 'receive'){
				
				if(it.springBean instanceof EventHandler && it.springBean.input.adapter instanceof HttpAdapter) {
					 
					// generate interface Gateway
					def interfaceName = it.springBean.name
					def springName = interfaceName.substring(0,1).toUpperCase() + interfaceName.substring(1)
					interfaceName = 'myservice.' + springName + "Gateway"
					JCodeModel codeModel = new JCodeModel();
					JDefinedClass gateWayInterface = codeModel._class(JMod.PUBLIC, interfaceName, ClassType.INTERFACE)
					JMethod method = gateWayInterface.method(JMod.PUBLIC, void.class, "method")
					JVar param = method.param(Object.class, "object")
					s = "." + File.separator + "src" + File.separator + "main" + File.separator + "java"
					//codeModel.build(new File("./src/main/java"))
					codeModel.build(new File(s))
					
					log.info interfaceName  + " generated"
					
					// generate Rest web service
					codeModel = new JCodeModel();
					def className = 'generated.' + springName + "RestWebService"
					JDefinedClass webService = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS)
					webService.annotate(RestController.class)
					JFieldVar field = webService.field(JMod.PRIVATE, gateWayInterface, "gateway");
					field.annotate(Autowired.class)
					
					
					def methodName = it.variable
					method = webService.method(JMod.PUBLIC, void.class, methodName)
					method.annotate(codeModel.ref("org.springframework.web.bind.annotation.RequestMapping")).param("value", "/microserviceUrl").param("method", RequestMethod.PUT);
					param = method.param(Integer.class, "i")
					param.annotate(RequestBody.class)
					
					JInvocation gatewayInvocation = JExpr.invoke(field, "method")
					gatewayInvocation.arg(param)
					
					JBlock body = method.body();
					body.add(gatewayInvocation)
					
					//codeModel.build(new File("./src/main/java"))
					codeModel.build(new File(s))
					
					log.info className  + " generated"
					
					// main spring boot
/*					codeModel = new JCodeModel();
					className = 'generated.' + "MainApplication"
					JDefinedClass mainClass = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS)
					mainClass.annotate(SpringBootApplication.class)
					mainClass.annotate(ImportResource.class)*/
					
				}
			}
		}
		
	}
	
	def generateConfiguration(def applications, String configurationFileName){
		
		log.info "generate Java Code for configuration: " + configurationFileName
		
		JCodeModel codeModel = new JCodeModel();
		def className = 'configuration.' + configurationFileName	//.substring(0, configurationFileName.lastIndexOf('.'))
		JDefinedClass configClass = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS)
		configClass.annotate(org.springframework.context.annotation.Configuration.class)
		
		applications.each{

			def methodName = it.name
			
			JMethod method
			
			if(it.input!=null && it.input.adapter.adapter == "JavaApplication"){
				method = configClass.method(JMod.PUBLIC, Application.class, methodName)
			} else if(it.input!=null && it.input.adapter.adapter == "File"){
				method = configClass.method(JMod.PUBLIC, EventHandler.class, methodName)
			} else if(it.output!=null && it.output.adapter.adapter == "File"){
				method = configClass.method(JMod.PUBLIC, EventHandler.class, methodName)
			} else if(it.output!=null && it.output.adapter.adapter == "SQL"){
				method = configClass.method(JMod.PUBLIC, EventHandler.class, methodName)
			}
			
			method.annotate(codeModel.ref(Bean.class));
			
			JBlock body = method.body();
			
			JVar applicationVar
			
			if(it.input!=null && it.input.adapter.adapter == "JavaApplication"){
				applicationVar = body.decl(codeModel.ref(Application.class), it.name, JExpr._new(codeModel.ref(Application.class)))
			} else if(it.input!=null && it.input.adapter.adapter == "File"){
				applicationVar = body.decl(codeModel.ref(EventHandler.class), it.name, JExpr._new(codeModel.ref(EventHandler.class)))
			} else if(it.output!=null && it.output.adapter.adapter == "File"){
				applicationVar = body.decl(codeModel.ref(EventHandler.class), it.name, JExpr._new(codeModel.ref(EventHandler.class)))
			} else if(it.output!=null && it.output.adapter.adapter == "SQL"){
				applicationVar = body.decl(codeModel.ref(EventHandler.class), it.name, JExpr._new(codeModel.ref(EventHandler.class)))
			}
			
			JInvocation invocation = JExpr.invoke(applicationVar, "setName")
			invocation.arg(it.name)
			body.add(invocation)
			
			if(it.input!=null && it.input.adapter.adapter == "JavaApplication"){

				invocation = JExpr.invoke(applicationVar, "setLanguage")
				invocation.arg(it.language)
				body.add(invocation)
				
				if(it.language=="Java" || it.language=="Groovy"){
					
					JVar adapter = body.decl(codeModel.ref(JavaServiceAdapter.class), "javaAdapter", JExpr._new(codeModel.ref(JavaServiceAdapter.class)))
					
					invocation = JExpr.invoke(adapter, "setJavaClass")
					invocation.arg(it.input.adapter.javaClass)
					body.add(invocation)
					
					invocation = JExpr.invoke(adapter, "setMethod")
					invocation.arg(it.input.adapter.method)
					body.add(invocation)
					
					JVar var = body.decl(codeModel.ref(Input.class), "input", JExpr._new(codeModel.ref(Input.class)))
					
					invocation = JExpr.invoke(var, "setType")
					invocation.arg(it.input.type)
					body.add(invocation)
					
					invocation = JExpr.invoke(var, "setAdapter")
					invocation.arg(adapter)
					body.add(invocation)
					
					invocation = JExpr.invoke(applicationVar, "setInput")
					invocation.arg(var)
					body.add(invocation)
					
					var = body.decl(codeModel.ref(Output.class), "output", JExpr._new(codeModel.ref(Output.class)))
					
					invocation = JExpr.invoke(var, "setType")
					invocation.arg(it.output.type)
					body.add(invocation)
					
					invocation = JExpr.invoke(var, "setAdapter")
					invocation.arg(adapter)
					body.add(invocation)
		
					invocation = JExpr.invoke(applicationVar, "setOutput")
					invocation.arg(var)
					body.add(invocation)
		
				}
			} else if(it.input!=null && it.input.adapter.adapter == "File"){
				
					JVar adapter = body.decl(codeModel.ref(InputFileAdapter.class), "inputFileAdapter", JExpr._new(codeModel.ref(InputFileAdapter.class)))
					
					invocation = JExpr.invoke(adapter, "setDirectory")
					invocation.arg(it.input.adapter.directory)
					body.add(invocation)
					
					invocation = JExpr.invoke(adapter, "setFilenamePattern")
					invocation.arg(it.input.adapter.filenamePattern)
					body.add(invocation)
					
					JVar var = body.decl(codeModel.ref(Input.class), "input", JExpr._new(codeModel.ref(Input.class)))
					
					invocation = JExpr.invoke(var, "setType")
					invocation.arg(it.input.type)
					body.add(invocation)
					
					invocation = JExpr.invoke(var, "setMimeType")
					invocation.arg(it.input.mimeType)
					body.add(invocation)
					
					invocation = JExpr.invoke(var, "setAdapter")
					invocation.arg(adapter)
					body.add(invocation)
					
					invocation = JExpr.invoke(applicationVar, "setInput")
					invocation.arg(var)
					body.add(invocation)
									
			} else if(it.output!=null && it.output.adapter.adapter == "File"){
				
					JVar adapter = body.decl(codeModel.ref(OutputFileAdapter.class), "outputFileAdapter", JExpr._new(codeModel.ref(OutputFileAdapter.class)))
					
					invocation = JExpr.invoke(adapter, "setDirectory")
					invocation.arg(it.output.adapter.directory)
					body.add(invocation)
					
					String s = it.output.adapter.createDirectory==true ?  "true" : "false"
					JVar bool = body.decl(codeModel.ref(Boolean.class), "createDirectory", JExpr._new(codeModel.ref(Boolean.class)).arg(s))
					invocation = JExpr.invoke(adapter, "setCreateDirectory")
					invocation.arg(bool)
					body.add(invocation)
					
					s = it.output.adapter.appendNewLine==true ?  "true" : "false"
					bool = body.decl(codeModel.ref(Boolean.class), "appendNewLine", JExpr._new(codeModel.ref(Boolean.class)).arg(s))
					invocation = JExpr.invoke(adapter, "setAppendNewLine")
					invocation.arg(bool)
					body.add(invocation)
					
					String writingMode
					switch(it.output.adapter.writingMode){
						case "REPLACE": 
							writingMode = "org.olabdynamics.compose.WritingMode.REPLACE"
							break
						case "APPEND":
							writingMode = "org.olabdynamics.compose.WritingMode.APPEND"
							break
					}
					
					invocation = JExpr.invoke(adapter, "setWritingMode")
					invocation.arg(JExpr.direct(writingMode))
					body.add(invocation)
					
					invocation = JExpr.invoke(adapter, "setFilename")
					invocation.arg(it.output.adapter.filename)
					body.add(invocation)
					
					JVar var = body.decl(codeModel.ref(Output.class), "output", JExpr._new(codeModel.ref(Output.class)))
					
					invocation = JExpr.invoke(var, "setType")
					invocation.arg(it.output.type)
					body.add(invocation)
					
					invocation = JExpr.invoke(var, "setMimeType")
					invocation.arg(it.output.mimeType)
					body.add(invocation)
					
					invocation = JExpr.invoke(var, "setAdapter")
					invocation.arg(adapter)
					body.add(invocation)
					
					invocation = JExpr.invoke(applicationVar, "setOutput")
					invocation.arg(var)
					body.add(invocation)
									
			} else if(it.output!=null && it.output.adapter.adapter == "SQL"){
				
					JVar dataSource = body.decl(codeModel.ref(DataSource.class), "dataSource", JExpr._new(codeModel.ref(DataSource.class)))
					
					invocation = JExpr.invoke(dataSource, "setDriver")
					invocation.arg(it.output.adapter.dataSource.driver)
					body.add(invocation)
					
					invocation = JExpr.invoke(dataSource, "setUsername")
					invocation.arg(it.output.adapter.dataSource.username)
					body.add(invocation)
					
					invocation = JExpr.invoke(dataSource, "setPassword")
					invocation.arg(it.output.adapter.dataSource.password)
					body.add(invocation)
					
					invocation = JExpr.invoke(dataSource, "setUrl")
					invocation.arg(it.output.adapter.dataSource.url)
					body.add(invocation)
			
					JVar adapter = body.decl(codeModel.ref(DatabaseAdapter.class), "databaseAdapter", JExpr._new(codeModel.ref(DatabaseAdapter.class)))
					
					invocation = JExpr.invoke(adapter, "setDataSource")
					invocation.arg(dataSource)
					body.add(invocation)
					
					invocation = JExpr.invoke(adapter, "setRequest")
					invocation.arg(it.output.adapter.request)
					body.add(invocation)
					
					JVar var = body.decl(codeModel.ref(Output.class), "output", JExpr._new(codeModel.ref(Output.class)))
					
					invocation = JExpr.invoke(var, "setType")
					invocation.arg(it.output.type)
					body.add(invocation)
					
					invocation = JExpr.invoke(var, "setAdapter")
					invocation.arg(adapter)
					body.add(invocation)
					
					invocation = JExpr.invoke(applicationVar, "setOutput")
					invocation.arg(var)
					body.add(invocation)
									
			}
	
			
			body._return(applicationVar)
	
		}
							
		
		String s = "." + File.separator + "compose"
		FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(s))
		codeModel.build(fileCodeWriter)
		fileCodeWriter.close()
		
		log.info className  + " generated"
		
	}

}
