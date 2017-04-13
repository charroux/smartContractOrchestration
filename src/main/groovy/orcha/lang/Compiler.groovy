package orcha.lang

import orcha.lang.business.BusinessAdapter
import orcha.lang.business.BasicBusinessAdapter
import orcha.lang.compiler.Compile;
import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.compiler.referenceimpl.CompileServiceWithSpringIntegration
import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ErrorUnwrapper;
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.impl.OrchaCodeVisitor

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ImportResource;

@Configuration
//@EnableAutoConfiguration
@ComponentScan(basePackages=['orcha.lang.compiler','orcha.lang.business','configuration'])	
class Compiler {
	
	@Bean
	Compile compile(){
		return new CompileServiceWithSpringIntegration()
	}
	
	@Bean
	OrchaCodeParser composeCodeParser(){
		return new OrchaCodeVisitor()
	} 
	
	@Bean
	BusinessAdapter businessAdapter(){
		return new BasicBusinessAdapter()
	}
	
	@Bean
	QualityOfService qualityOfService(){
		return new QualityOfServiceImpl()
	}
	
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(Compiler.class)
		application.setWebEnvironment(false)
		application.run(args)
	}

}
