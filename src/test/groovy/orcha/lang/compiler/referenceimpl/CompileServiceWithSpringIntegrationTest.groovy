package orcha.lang.compiler.referenceimpl

import org.jdom2.Attribute
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.jdom2.xpath.XPathExpression
import org.jdom2.xpath.XPathFactory
import org.jdom2.filter.Filters
import org.junit.Test
import org.junit.Assert
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import service.callingServiceByEMail.Customer
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import orcha.lang.compiler.Compile
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import orcha.lang.configuration.Application
import orcha.lang.configuration.EventHandler
import orcha.lang.configuration.CircuitBreaker
import orcha.lang.configuration.Retry

@RunWith(SpringRunner.class)
@SpringBootTest
class CompileServiceWithSpringIntegrationTest {
	
	@Autowired
	OrchaCodeParser orchaCodeParser
	
	@Autowired
	Compile compile
	
	@Autowired
	EventHandler customer
	
	@Autowired
	Application prepareOrder
	
	@Autowired
	EventHandler delivery
	
	//the following concerns BenchmarkingExample tests
	@Autowired
	EventHandler benchmarkingInputFile
	
	@Autowired
	Application codeToBenchmark1
	
	@Autowired
	Application codeToBenchmark2
	
	@Autowired
	EventHandler benchmarkingOutputFile
	
	//the following concerns ComputeInSeries
	
	@Autowired
	Application firstProgram
	
	@Autowired
	EventHandler computesInSeriesInputFile
	
	@Autowired
	Application secondProgram
	
	@Autowired
	EventHandler computesInSeriesOutputFile
	
	// the following concerns javascriptService
	@Autowired
	EventHandler javascriptServiceInputFile
	
	@Autowired
	Application javascriptService
	
	@Autowired
	EventHandler javascriptServiceOutputFile
	
	// the following concerns RetryService
	@Autowired
	Application serviceWithRetry
	
	@Autowired
	EventHandler retryInputFile
	
	@Autowired
	EventHandler qosOutputFile
	
	// the following concerns Circuit breaker
	@Autowired
	Application serviceWithCircuitBreaker
	
	
	
	@Test
	void prepareOrder(){
	
		// the Orcha source program
			
		String orchaProgram = 	"package source.prepareOrder\n" +
		"domain productSales\n" +
		"description 'Prepare an order.'\n" +
		"title 'prepare order'\n" +
		"author 'Ben C.'\n" +
		"version '1.0'\n" +
		"receive order from customer\n" +
		"compute prepareOrder with order.value\n" + 
		"when 'prepareOrder terminates'\n" + 
		"send prepareOrder.result to delivery"
		
		// construct the graph of instructions for the Orcha programm
		
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		
		// generate an XML file (Spring integration configuration): this is the file to be tested
		 
		String path = "." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator		
		File destinationDirectory = new File(path)
		compile.compile(orchaCodeVisitor, destinationDirectory)
		
		String xmlSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + ".xml"
		String pathToXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextFileName
		
		// parse the XML file checking is correctness
		
		SAXBuilder builder = new SAXBuilder()
		
		Document xmlSpringIntegration = builder.build(pathToXmlFile)
		
		XPathFactory xFactory = XPathFactory.instance()

		// <int-file:inbound-channel-adapter id="file-customer-InputChannel-id" directory="data/input" channel="customer-InputChannel" prevent-duplicates="true" filename-pattern="orderToPrepare.json">		
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'inbound-channel-adapter']", Filters.element())
		List<Element> elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		Element element = elements.get(0)
		
		Assert.assertEquals(element.getAttribute("directory").getValue(), customer.input.adapter.directory)
		Assert.assertEquals(element.getAttribute("filename-pattern").getValue(), customer.input.adapter.filenamePattern)

		// <int:chain input-channel="customer-InputChannelTransformer" output-channel="customer-OutputChannel">
		// 		<int:json-to-object-transformer type="service.prepareOrder.Order" />
  		
		expr = xFactory.compile("//*[local-name() = 'chain']/*[local-name() = 'json-to-object-transformer']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
		
		Assert.assertEquals(element.getAttribute("type").getValue(), customer.input.type)
			
		// <int:chain input-channel="customer-InputChannelTransformer" output-channel="customer-OutputChannel">
		//		<int:header-enricher>
		//			<int:header name="messageID" expression="headers['id'].toString()" />
	  
		expr = xFactory.compile("//*[local-name() = 'chain']/*[local-name() = 'header-enricher']/*[local-name() = 'header']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
		 
		Assert.assertEquals(element.getAttribute("name").getValue(), "messageID")
		Assert.assertEquals(element.getAttribute("expression").getValue(), "headers['id'].toString()")
		 
		// <int:chain input-channel="customer-OutputChannel" output-channel="prepareOrderServiceAcivatorOutput" id="service-activator-chain-prepareOrderChannel-id">
		//		<int:service-activator id="service-activator-prepareOrderChannel-id" expression="@orderPreparation.prepare(payload)">
		
		expr = xFactory.compile("//*[local-name() = 'chain']/*[local-name() = 'service-activator']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
		 
		Assert.assertTrue(element.getAttribute("expression").getValue().contains(prepareOrder.input.adapter.method))
		 
		// <int:chain input-channel="customer-OutputChannel" output-channel="prepareOrderServiceAcivatorOutput" id="service-activator-chain-prepareOrderChannel-id">
		//		<int:service-activator id="service-activator-prepareOrderChannel-id" expression="@orderPreparation.prepare(payload)">
		//			<int:request-handler-a dvice-chain>
		//				<bean class="org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice">
		//					<property name="threshold" value="2" />
		//					<property name="halfOpenAfter" value="2000" />
		
		expr = xFactory.compile("//*[local-name() = 'chain']/*[local-name() = 'service-activator']/*[local-name() = 'request-handler-advice-chain']/*[local-name() = 'bean']/*[local-name() = 'property']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 2)
		
		Class<?> beanClass = prepareOrder.getClass()
		
		Assert.assertTrue(beanClass.isAnnotationPresent(CircuitBreaker.class))
		
		element = elements.get(0)
		int numberOfFailuresBeforeOpening = beanClass.getAnnotation(CircuitBreaker.class).numberOfFailuresBeforeOpening()
		Assert.assertEquals(element.getAttribute("value").getValue(), numberOfFailuresBeforeOpening.toString())
		
		element = elements.get(1)
		long intervalBeforeHalfOpening = beanClass.getAnnotation(CircuitBreaker.class).intervalBeforeHalfOpening()
		Assert.assertEquals(element.getAttribute("value").getValue(), intervalBeforeHalfOpening.toString())		
		
		// <int:chain input-channel="customer-OutputChannel" output-channel="prepareOrderServiceAcivatorOutput" id="service-activator-chain-prepareOrderChannel-id">
		//		<int:service-activator id="service-activator-prepareOrderChannel-id" expression="@orderPreparation.prepare(payload)">
		//			<int:request-handler-advice-chain>
		//				<int:retry-advice max-attempts="3" recovery-channel="recoveryChannel">
		//					<int:exponential-back-off initial="5000" multiplier="2" maximum="20000" />
		
		expr = xFactory.compile("//*[local-name() = 'chain']/*[local-name() = 'service-activator']/*[local-name() = 'request-handler-advice-chain']/*[local-name() = 'retry-advice']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		 
		beanClass = prepareOrder.getClass()
		 
		Assert.assertTrue(beanClass.isAnnotationPresent(Retry.class))
		 
		element = elements.get(0)		
		
		int maxNumberOfAttempts = beanClass.getAnnotation(Retry.class).maxNumberOfAttempts()
		Assert.assertEquals(element.getAttribute("max-attempts").getValue(), maxNumberOfAttempts.toString())
		
		elements = element.getChildren()
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
		
		long intervalBetweenTheFirstAndSecondAttempt = beanClass.getAnnotation(Retry.class).intervalBetweenTheFirstAndSecondAttempt()
		Assert.assertEquals(element.getAttribute("initial").getValue(), intervalBetweenTheFirstAndSecondAttempt.toString())
		
		int intervalMultiplierBetwennAttemps = beanClass.getAnnotation(Retry.class).intervalMultiplierBetweenAttemps()
		Assert.assertEquals(element.getAttribute("multiplier").getValue(), intervalMultiplierBetwennAttemps.toString())
		
		int maximumIntervalBetweenAttempts = beanClass.getAnnotation(Retry.class).maximumIntervalBetweenAttempts()
		Assert.assertEquals(element.getAttribute("maximum").getValue(), maximumIntervalBetweenAttempts.toString())

		// <int:transformer id="transformer-prepareOrderServiceAcivatorOutput-id" input-channel="prepareOrderServiceAcivatorOutput" output-channel="prepareOrderAggregatorInput" method="transform">
		//		<bean class="orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer">
		//			<property name="application" ref="prepareOrder" />
	  
		expr = xFactory.compile("//*[local-name() = 'transformer']/*[local-name() = 'bean'][@class='orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer']/*[local-name() = 'property']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
		Assert.assertEquals(element.getAttribute("name").getValue(), "application")
		Assert.assertEquals(element.getAttribute("ref").getValue(), prepareOrder.name)
	
		// <int:aggregator id="aggregator-prepareOrderAggregatorInput-id" input-channel="prepareOrderAggregatorInput" output-channel="prepareOrderAggregatorInputTransformer" release-strategy-expression="size()==1 and ( ([0].payload instanceof T(orcha.lang.configuration.Application) AND [0].payload.state==T(orcha.lang.configuration.State).TERMINATED) )" correlation-strategy-expression="headers['messageID']" />
		// <int:transformer id="transformer-prepareOrderAggregatorInput-id" input-channel="prepareOrderAggregatorInputTransformer" output-channel="prepareOrderAggregatorInputAggregatorOutput" expression="payload.?[name=='prepareOrder']" />
		
		expr = xFactory.compile("//*[local-name() = 'aggregator']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
		String outputChannel = element.getAttribute("output-channel").getValue()
		println outputChannel
		expr = xFactory.compile("//*[local-name() = 'transformer'][@input-channel='" + outputChannel + "']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		 	
		// <int:transformer id="transformer-prepareOrderAggregatorInputAggregatorOutput-id" input-channel="prepareOrderAggregatorInputAggregatorOutput" output-channel="prepareOrderAggregatorOutputTransformer" method="transform">
		//		<bean class="orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ApplicationToObjectTransformer" />
  	 
		expr = xFactory.compile("//*[local-name() = 'transformer']/*[local-name() = 'bean'][@class='orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ApplicationToObjectTransformer']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		 
		//<int:chain input-channel="prepareOrderAggregatorOutputTransformer" output-channel="prepareOrderOutputFileChannelAdapterdelivery">
		//		<int:object-to-json-transformer />
	  
		expr = xFactory.compile("//*[local-name() = 'chain']/*[local-name() = 'object-to-json-transformer']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		 
		// <int-file:outbound-channel-adapter id="file-prepareOrderdeliveryChannel-id" channel="prepareOrderOutputFileChannelAdapterdelivery" directory-expression="@delivery.output.adapter.directory" filename-generator-expression="@delivery.output.adapter.filename" append-new-line="true" mode="REPLACE" auto-create-directory="true" delete-source-files="false" />

		expr = xFactory.compile("//*[local-name() = 'outbound-channel-adapter']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
		Assert.assertEquals(element.getAttribute("directory-expression").getValue(), '@' + delivery.name + '.output.adapter.directory')
		Assert.assertEquals(element.getAttribute("filename-generator-expression").getValue(), '@' + delivery.name + '.output.adapter.filename')
		
		Assert.assertTrue(new File(pathToXmlFile).delete())
	
		String xmlQoSSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + "QoS.xml"
		String pathToQoSXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlQoSSpringContextFileName
		
		Assert.assertTrue(new File(pathToQoSXmlFile).delete())
		
	}
	
	@Test
	void benchmarkingExample(){
		
		String orchaProgram = 	"title 'benchmarking services'\n" +
		"description 'Read a tesxt file, dispatch its content to two services, then launch the two services and wait until the two services complete. Write the result to a service into a file.'\n" +
		"receive event from benchmarkingInputFile\n" +
		"compute codeToBenchmark1 with event.value\n" +
		"receive event from benchmarkingInputFile\n" +
		"compute codeToBenchmark2 with event.value\n" +
		"when 'codeToBenchmark1 terminates and codeToBenchmark2 terminates'\n" +
		"send codeToBenchmark1.result to benchmarkingOutputFile"
		
		// construct the graph of instructions for the Orcha programm
		
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		
		// generate an XML file (Spring integration configuration): this is the file to be tested
		 
		String path = "." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
		File destinationDirectory = new File(path)
		compile.compile(orchaCodeVisitor, destinationDirectory)
		
		String xmlSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + ".xml"
		String pathToXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextFileName
		
		// parse the XML file checking is correctness
		
		SAXBuilder builder = new SAXBuilder()
		
		Document xmlSpringIntegration = builder.build(pathToXmlFile)
		
		XPathFactory xFactory = XPathFactory.instance()
		
		// <int-file:inbound-channel-adapter channel="benchmarkingInputFile-InputChannel">
		// <int-file:file-to-string-transformer input-channel="benchmarkingInputFile-InputChannel"/>
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'inbound-channel-adapter']", Filters.element())
		List<Element> elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		Element element = elements.get(0)
		
		XPathExpression<Element> expr2 = xFactory.compile("//*[local-name() = 'file-to-string-transformer']", Filters.element())
		 List<Element> elements2 = expr2.evaluate(xmlSpringIntegration)
		 Assert.assertTrue(elements2.size() == 1)
		 Element element2 = elements2.get(0)
		 
		Assert.assertEquals(element.getAttribute("channel").getValue(), element2.getAttribute("input-channel").getValue())

		// <int-file:file-to-string-transformer output-channel="benchmarkingInputFile-InputChannelTransformer"/>
		// <int:chain input-channel="benchmarkingInputFile-InputChannelTransformer">
	  
		expr = xFactory.compile("//*[local-name() = 'chain']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		element = elements.get(0)
		 
		Assert.assertEquals(element.getAttribute("input-channel").getValue(), element2.getAttribute("output-channel").getValue())
		
		// <int:chain output-channel="benchmarkingInputFile-OutputChannel">
		// <int:recipient-list-router input-channel="benchmarkingInputFile-OutputChannel">

		 expr2 = xFactory.compile("//*[local-name() = 'recipient-list-router']", Filters.element())
		 elements2 = expr2.evaluate(xmlSpringIntegration)
		 Assert.assertTrue(elements2.size() == 1)
		 element2 = elements2.get(0)
		 
		 Assert.assertEquals(element.getAttribute("output-channel").getValue(), element2.getAttribute("input-channel").getValue())
		 
		 
		// <int:transformer id="transformer-codeToBenchmark2ServiceAcivatorOutput-id" input-channel="codeToBenchmark2ServiceAcivatorOutput" output-channel="codeToBenchmark1codeToBenchmark2AggregatorInput" method="transform">
		//		<bean class="orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer">
		//  <property name="application" ref="codeToBenchmark2" />
	  
		expr = xFactory.compile("//*[local-name() = 'transformer']/*[local-name() = 'bean'][@class='orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ObjectToApplicationTransformer']/*[local-name() = 'property']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 2)
		element = elements.get(0)
		Assert.assertEquals(element.getAttribute("name").getValue(), "application")
		Assert.assertEquals(element.getAttribute("ref").getValue(), codeToBenchmark1.name)
		element = elements.get(1)
		Assert.assertEquals(element.getAttribute("name").getValue(), "application")
		Assert.assertEquals(element.getAttribute("ref").getValue(), codeToBenchmark2.name)

		// <int:aggregator input-channel="codeToBenchmark1codeToBenchmark2AggregatorInput"/>
		// <int:transformer output-channel="codeToBenchmark1codeToBenchmark2AggregatorInput">
	  		
		expr = xFactory.compile("//*[local-name() = 'aggregator']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
		
		expr2 = xFactory.compile("//*[local-name() = 'transformer']", Filters.element())
		elements2 = expr2.evaluate(xmlSpringIntegration)
		element2 = elements2.get(1)
		
		Assert.assertEquals(element.getAttribute("input-channel").getValue(), element2.getAttribute("output-channel").getValue())
		 
		//  <int:chain input-channel="codeToBenchmark1codeToBenchmark2AggregatorOutputTransformer" output-channel="codeToBenchmark1OutputFileChannelAdapterbenchmarkingOutputFile">
		//		<int:object-to-string-transformer />
	  
		expr = xFactory.compile("//*[local-name() = 'chain']/*[local-name() = 'object-to-string-transformer']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		 
		//   <int-file:outbound-channel-adapter directory-expression="@benchmarkingOutputFile.output.adapter.directory" />

		expr = xFactory.compile("//*[local-name() = 'outbound-channel-adapter']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
		Assert.assertEquals(element.getAttribute("directory-expression").getValue(), '@' + benchmarkingOutputFile.name + '.output.adapter.directory')
		Assert.assertEquals(element.getAttribute("filename-generator-expression").getValue(), '@' + benchmarkingOutputFile.name + '.output.adapter.filename')
		Assert.assertEquals(element.getAttribute("delete-source-files").getValue(), 'false')
		Assert.assertEquals(element.getAttribute("auto-create-directory").getValue(), 'true')
		Assert.assertEquals(element.getAttribute("append-new-line").getValue(), 'true')
		Assert.assertEquals(element.getAttribute("mode").getValue(), 'REPLACE')
		
		//  <int:aggregator release-strategy-expression="size()==2 and ( ([0].payload instanceof T(orcha.lang.configuration.Application) AND [0].payload.state==T(orcha.lang.configuration.State).TERMINATED)  and  ([1].payload instanceof T(orcha.lang.configuration.Application) AND [1].payload.state==T(orcha.lang.configuration.State).TERMINATED) )"/>

		expr = xFactory.compile("//*[local-name() = 'aggregator']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		element = elements.get(0)
		
		expr2 = xFactory.compile("//*[local-name() = 'service-activator']", Filters.element())
		elements2 = expr2.evaluate(xmlSpringIntegration)
		int size = elements2.size()
		
		Assert.assertTrue(element.getAttribute("release-strategy-expression").getValue().contains("size()=="+size))
		int i = 0
		while (i!=size) {
			Assert.assertTrue(element.getAttribute("release-strategy-expression").getValue().contains("([" + i + "].payload instanceof T(orcha.lang.configuration.Application) AND [" + i + "].payload.state==T(orcha.lang.configuration.State).TERMINATED)"))
			i++
		}
		
		Assert.assertTrue(new File(pathToXmlFile).delete())
	
		String xmlQoSSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + "QoS.xml"
		String pathToQoSXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlQoSSpringContextFileName
		
		Assert.assertTrue(new File(pathToQoSXmlFile).delete())
	}
	
	@Test
	void ComputesInSeries(){
		
		String orchaProgram = 	"title 'computes in series'\n"+
		"description 'Read the content of a text file. Pass its content to a service. Launch another service in a serie. Then write the result of the lastest service to a file.'\n"+
		"receive event from computesInSeriesInputFile\n"+
		"compute firstProgram with event.value\n"+
		"when 'firstProgram terminates'\n"+
		"compute secondProgram with firstProgram.result\n"+
		"when 'secondProgram terminates'\n"+
		"send secondProgram.result to computesInSeriesOutputFile"
		
		// construct the graph of instructions for the Orcha programm
		
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		
		// generate an XML file (Spring integration configuration): this is the file to be tested
		 
		String path = "." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
		File destinationDirectory = new File(path)
		compile.compile(orchaCodeVisitor, destinationDirectory)
		
		String xmlSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + ".xml"
		String pathToXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextFileName
		
		// parse the XML file checking is correctness
		
		SAXBuilder builder = new SAXBuilder()
		
		Document xmlSpringIntegration = builder.build(pathToXmlFile)
		
		XPathFactory xFactory = XPathFactory.instance()
				 
		// <int:service-activator expression="@program1.myMethod(payload)">
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'service-activator']", Filters.element())
		List<Element> elements = expr.evaluate(xmlSpringIntegration)
		
		Element element = elements.get(0)
		def s = firstProgram.input.adapter.javaClass
		def values = s.tokenize( '.' )
		int sizeValues = values.size()
		def expression = "@"+values[sizeValues-1]+"."+firstProgram.input.adapter.method+"(payload)"
		Assert.assertEquals(element.getAttribute("expression").getValue().toLowerCase() , expression.toLowerCase())

		element = elements.get(1)
		s = secondProgram.input.adapter.javaClass
		values = s.tokenize( '.' )
		sizeValues = values.size()
		expression = "@"+values[sizeValues-1]+"."+secondProgram.input.adapter.method+"(payload)"
		Assert.assertEquals(element.getAttribute("expression").getValue().toLowerCase() , expression.toLowerCase())
		
		//	<int:transformer id="transformer-firstProgramAggregatorInputAggregatorOutput-id" input-channel="firstProgramAggregatorInputAggregatorOutput" output-channel="firstProgramAggregatorOutputTransformer" method="transform">
		//	<int:chain input-channel="firstProgramAggregatorOutputTransformer" output-channel="secondProgramServiceAcivatorOutput" id="service-activator-chain-secondProgramChannel-id">
		
		def id = "transformer-"+firstProgram.name+"AggregatorInputAggregatorOutput-id"
		expr = xFactory.compile("//*[local-name() = 'transformer'][@id='"+id+"']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		element = elements.get(0)
			
		id = "service-activator-chain-"+secondProgram.name+"Channel-id"
		XPathExpression<Element> expr2 = xFactory.compile("//*[local-name() = 'chain'][@id='"+id+"']", Filters.element())
		List<Element> elements2 = expr2.evaluate(xmlSpringIntegration)
		Element element2 = elements2.get(0)
		
		Assert.assertEquals(element.getAttribute("output-channel").getValue() , element2.getAttribute("input-channel").getValue())
		
		Assert.assertTrue(new File(pathToXmlFile).delete())
		String xmlQoSSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + "QoS.xml"
		String pathToQoSXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlQoSSpringContextFileName
		Assert.assertTrue(new File(pathToQoSXmlFile).delete())
	}
	
	@Test
	void JavascriptExample(){
		
		String orchaProgram = "package source.javascript\n"+
		"title 'javascript service'\n"+
		"description 'Read a Json file. Pass its content to a Javascript service. Launch the service, then write the result of the service to a Json file.'\n"+
		"receive event from javascriptServiceInputFile\n"+
		"compute javascriptService with event.value\n"+
		"when 'javascriptService terminates'\n"+
		"send javascriptService.result to javascriptServiceOutputFile"
				
		// construct the graph of instructions for the Orcha programm
		
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		
		// generate an XML file (Spring integration configuration): this is the file to be tested
		 
		String path = "." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
		File destinationDirectory = new File(path)
		compile.compile(orchaCodeVisitor, destinationDirectory)
		
		String xmlSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + ".xml"
		String pathToXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextFileName
		
		// parse the XML file checking is correctness
		
		SAXBuilder builder = new SAXBuilder()
		
		Document xmlSpringIntegration = builder.build(pathToXmlFile)
		
		XPathFactory xFactory = XPathFactory.instance()
				 
		// <int:service-activator expression="@program1.myMethod(payload)">
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'service-activator']/*[local-name() = 'script']", Filters.element())
		List<Element> elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		Element element = elements.get(0)
		Assert.assertEquals(element.getAttribute("lang").getValue(),"js")
		Assert.assertTrue(element.getAttribute("location").getValue().endsWith(".js"))
		Assert.assertEquals(element.getAttribute("location").getValue(),javascriptService.input.adapter.file)
		
		// <int:json-to-object-transformer type="service.javascript.Person" />
		expr = xFactory.compile("//*[local-name() = 'json-to-object-transformer']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		element = elements.get(0)
    	Assert.assertEquals(element.getAttribute("type").getValue(),javascriptService.input.type)
		
		// <int-file:file-to-string-transformer input-channel="javascriptServiceInputFile-InputChannel" output-channel="javascriptServiceInputFile-InputChannelTransformer" delete-files="false" />
		// <int:chain input-channel="javascriptServiceInputFile-InputChannelTransformer" output-channel="javascriptServiceInputFile-OutputChannel">
	  
		expr = xFactory.compile("//*[local-name() = 'json-to-object-transformer']/..", Filters.element())
		 elements = expr.evaluate(xmlSpringIntegration)
		 element = elements.get(0)
		 
		 XPathExpression<Element> expr2 = xFactory.compile("//*[local-name() = 'file-to-string-transformer']", Filters.element())
		  List<Element> elements2 = expr2.evaluate(xmlSpringIntegration)
		  Element element2 = elements2.get(0)
		 
		 Assert.assertEquals(element.getAttribute("input-channel").getValue(), element2.getAttribute("output-channel").getValue())
		  
		//  <int:transformer output-channel="javascriptServiceAggregatorOutputTransformer" method="transform">
		//   <int:chain input-channel="javascriptServiceAggregatorOutputTransformer" output-channel="javascriptServiceOutputFileChannelAdapterjavascriptServiceOutputFile">

		expr = xFactory.compile("//*[local-name() = 'object-to-json-transformer']/..", Filters.element())
		 elements = expr.evaluate(xmlSpringIntegration)
		 element = elements.get(0)
		 
		 def id = "transformer-"+javascriptService.name+"AggregatorInputAggregatorOutput-id"
		 expr2 = xFactory.compile("//*[local-name() = 'transformer'][@id='"+id+"']", Filters.element())
		 elements2 = expr2.evaluate(xmlSpringIntegration)
		 element2 = elements2.get(0)
		  
		 Assert.assertEquals(element.getAttribute("input-channel").getValue(), element2.getAttribute("output-channel").getValue())
		 
		 //<int:object-to-json-transformer />
		 expr = xFactory.compile("//*[local-name() = 'object-to-json-transformer']", Filters.element())
		  elements = expr.evaluate(xmlSpringIntegration)
		  Assert.assertTrue(elements.size()==1)
		 
		
		
				
		Assert.assertTrue(new File(pathToXmlFile).delete())
		String xmlQoSSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + "QoS.xml"
		String pathToQoSXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlQoSSpringContextFileName
		
		Assert.assertTrue(new File(pathToQoSXmlFile).delete())
	}
	
	@Test
	void Retry(){
		
		String orchaProgram = "package source.qos\n"+
		"title 'retry'\n"+
		"description 'Use the retry pattern. The service is automatically launch again 3 times. The two first times the service throws an exception. The third time, the service completes.'\n"+
		"receive event from retryInputFile\n"+
		"compute serviceWithRetry with event.value\n"+
		"when 'serviceWithRetry terminates'\n"+
		"send serviceWithRetry.result to qosOutputFile"
				
		// construct the graph of instructions for the Orcha programm
		
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		
		// generate an XML file (Spring integration configuration): this is the file to be tested
		 
		String path = "." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
		File destinationDirectory = new File(path)
		compile.compile(orchaCodeVisitor, destinationDirectory)
		
		String xmlSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + ".xml"
		String pathToXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextFileName
		
		// parse the XML file checking is correctness
		
		SAXBuilder builder = new SAXBuilder()
		
		Document xmlSpringIntegration = builder.build(pathToXmlFile)
		
		XPathFactory xFactory = XPathFactory.instance()
				 
		// <int:exponential-back-off initial="1000" multiplier="2" maximum="4000" />
		XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'service-activator']//*[local-name() = 'exponential-back-off']", Filters.element())
		List<Element> elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		Element element = elements.get(0)
		Assert.assertEquals(element.getAttribute("multiplier").getValue(),"2")
		Assert.assertEquals(element.getAttribute("initial").getValue(),"1000")
		Assert.assertEquals(element.getAttribute("maximum").getValue(),"4000")
		
		// the presence of <int:request-handler-advice-chain>
		expr = xFactory.compile("//*[local-name() = 'service-activator']//*[local-name() = 'request-handler-advice-chain']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
		
		// the presence of <int:retry-advice>
		expr = xFactory.compile("//*[local-name() = 'service-activator']//*[local-name() = 'retry-advice']", Filters.element())
		elements = expr.evaluate(xmlSpringIntegration)
		Assert.assertTrue(elements.size() == 1)
				
		Assert.assertTrue(new File(pathToXmlFile).delete())
		String xmlQoSSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + "QoS.xml"
		String pathToQoSXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlQoSSpringContextFileName
		
		Assert.assertTrue(new File(pathToQoSXmlFile).delete())
	}
	
	@Test
	void CircuitBreaker(){
	
		// the Orcha source program
			
		String orchaProgram = 	"package source.qos\n"+
		"description 'Use the circuit breaker pattern. Read all files whose names start with circuitBreakerInputFile at the rate of one file per second. Then pass the content of each file to a service. The service fails the two first times. Then the circuit breaker in opened. The thrid attemps to launch the service occurs before the circuit breaker returns to the half opened state, so the service is no more call.'\n"+
		"title 'circuit breaker'\n"+
		"receive event from circuitBreakerInputFile\n"+
		"compute serviceWithCircuitBreaker with event.value\n"+
		"when 'serviceWithCircuitBreaker terminates'\n"+
		"send serviceWithCircuitBreaker.result to qosOutputFile"

		
		// construct the graph of instructions for the Orcha programm
		
		OrchaCodeVisitor orchaCodeVisitor = orchaCodeParser.parse(orchaProgram)
		
		// generate an XML file (Spring integration configuration): this is the file to be tested
		 
		String path = "." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
		File destinationDirectory = new File(path)
		compile.compile(orchaCodeVisitor, destinationDirectory)
		
		String xmlSpringContextFileName = orchaCodeVisitor.getOrchaMetadata().getTitle() + ".xml"
		String pathToXmlFile = destinationDirectory.getAbsolutePath() + File.separator + xmlSpringContextFileName
		
		// parse the XML file checking is correctness
		
		SAXBuilder builder = new SAXBuilder()
		Document xmlSpringIntegration = builder.build(pathToXmlFile)
		XPathFactory xFactory = XPathFactory.instance()
		
		//<int:service-activator id="service-activator-serviceWithCircuitBreakerChannel-id" expression="@service.myMethod(payload)">
		//<int:request-handler-advice-chain>
		//  <bean class="org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice">
		//	<property name="threshold" value="2" />
		//	<property name="halfOpenAfter" value="5000" />
		
		 XPathExpression<Element> expr = xFactory.compile("//*[local-name() = 'chain']/*[local-name() = 'service-activator']/*[local-name() = 'request-handler-advice-chain']/*[local-name() = 'bean']/*[local-name() = 'property']", Filters.element())
		 List<Element> elements = expr.evaluate(xmlSpringIntegration)
		 Assert.assertTrue(elements.size() == 2)
		 
		 Class<?> beanClass = serviceWithCircuitBreaker.getClass()
		 
		 Assert.assertTrue(beanClass.isAnnotationPresent(CircuitBreaker.class))
		 
		 Element element = elements.get(0)
		 int numberOfFailuresBeforeOpening = beanClass.getAnnotation(CircuitBreaker.class).numberOfFailuresBeforeOpening()
		 Assert.assertEquals(element.getAttribute("value").getValue(), numberOfFailuresBeforeOpening.toString())
		 
		 element = elements.get(1)
		 long intervalBeforeHalfOpening = beanClass.getAnnotation(CircuitBreaker.class).intervalBeforeHalfOpening()
		 Assert.assertEquals(element.getAttribute("value").getValue(), intervalBeforeHalfOpening.toString())
	}

		
}
