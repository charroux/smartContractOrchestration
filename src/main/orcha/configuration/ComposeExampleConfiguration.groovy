package configuration

import orcha.lang.configuration.Application
import orcha.lang.configuration.Input
import orcha.lang.configuration.JavaServiceAdapter
import orcha.lang.configuration.Output
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import service.GroovyCode1
import service.GroovyCode2


@Configuration
class ComposeConfiguration {
	
	@Bean
	GroovyCode1 groovyCode1(){
		return new GroovyCode1()
	}
	
	@Bean
	Application code1(){
		def code1Application = new Application(name: "code1", language: "Groovy")
		def groovyCode1Adapter = new JavaServiceAdapter(javaClass: 'GroovyCode1', method:'method1')
		def code1Input = new Input(type: "java.lang.Integer", adapter: groovyCode1Adapter)
		code1Application.input = code1Input
		def code1Output = new Output(type: "java.lang.Integer", adapter: groovyCode1Adapter)
		code1Application.output = code1Output
		return code1Application
	}

	@Bean
	GroovyCode2 groovyCode2(){
		return new GroovyCode2()
	}
	
	@Bean
	Application code2(){
		def code2Application = new Application(name: "code2", language: "Groovy")
		def groovyCode2Adapter = new JavaServiceAdapter(javaClass: 'GroovyCode2', method:'method2')
		def code2Input = new Input(type: "java.lang.Integer", adapter: groovyCode2Adapter)
		code2Application.input = code2Input
		def code2Output = new Output(type: "java.lang.Integer", adapter: groovyCode2Adapter)
		code2Application.output = code2Output
		return code2Application
	}
	

}
