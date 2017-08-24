package orcha.lang.compiler.referenceimpl.xmlgenerator

import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.referenceimpl.ExpressionParserImpl
import orcha.lang.compiler.referenceimpl.xmlgenerator.connectors.SpringIntegrationConnectors
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.InboundChannelAdapter
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.JDom2XmlGeneratorForSpringIntegration
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.Poller
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.Transformer
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.XmlGeneratorForSpringIntegration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class XmlGeneratorConfiguration {
	
	@Bean
	XmlGenerator xmlGenerator(){
		//return new XmlGeneratorForSpringIntegration()
		return new JDom2XmlGeneratorForSpringIntegration()
	}
	
}
