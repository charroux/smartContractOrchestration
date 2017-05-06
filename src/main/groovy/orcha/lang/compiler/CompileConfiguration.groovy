package orcha.lang.compiler

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import orcha.lang.business.BasicBusinessAdapter
import orcha.lang.business.BusinessAdapter
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.compiler.referenceimpl.CompileServiceWithSpringIntegration
import orcha.lang.compiler.referenceimpl.ExpressionParser
import orcha.lang.compiler.referenceimpl.ExpressionParserImpl
import orcha.lang.compiler.referenceimpl.OrchaLauncherGenerator
import orcha.lang.compiler.referenceimpl.configurationproperties.ConfigurationPropertiesGenerator
import orcha.lang.compiler.referenceimpl.testing.ConfigurationMockGenerator
import orcha.lang.compiler.referenceimpl.xmlgenerator.XmlGenerator
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.XmlGeneratorForSpringIntegration
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.impl.OrchaCodeVisitor

@Configuration
//@Component
class CompileConfiguration {
	
	@Bean
	Compile compile(){
		return new CompileServiceWithSpringIntegration()
	}
	
	@Bean
	BusinessAdapter businessAdapter(){
		return new BasicBusinessAdapter()
	}
	
	@Bean
	OrchaCodeParser composeCodeParser(){
		return new OrchaCodeVisitor()
	}
	
	@Bean
	ExpressionParser ExpressionParser(){
		return new ExpressionParserImpl()
	}
	
	@Bean
	QualityOfService qualityOfService(){
		return new QualityOfServiceImpl()
	}
	
	@Bean
	ConfigurationMockGenerator configurationMockGenerator(){
		return new ConfigurationMockGenerator()
	}
	
	@Bean
	ConfigurationPropertiesGenerator configurationPropertiesGenerator(){
		return new ConfigurationPropertiesGenerator()
	}
	
	@Bean
	OrchaLauncherGenerator orchaLauncherGenerator(){
		return new OrchaLauncherGenerator()
	}
	
/*	@Bean
	XmlGenerator xmlGenerator(){
		return new XmlGeneratorForSpringIntegration()
	}*/

}
