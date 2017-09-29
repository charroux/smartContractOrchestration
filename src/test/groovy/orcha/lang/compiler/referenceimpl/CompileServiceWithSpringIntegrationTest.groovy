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
		//			<int:request-handler-advice-chain>
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
		// to be completed...
		// begin by add the orcha program from src/main/orcha/source/BenchmarkingExample
	}
	

}
