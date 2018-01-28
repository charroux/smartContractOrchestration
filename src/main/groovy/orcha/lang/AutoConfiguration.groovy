package orcha.lang

import orcha.lang.ImportDynamicResourcesForConfiguration
import orcha.lang.configuration.ConfigurableProperties
import orcha.lang.configuration.EventHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.bind.RelaxedNames;
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource

import groovy.util.logging.Slf4j
import java.lang.reflect.Field
import java.lang.reflect.Method
import orcha.lang.configure.ConfigurationProperties

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@Slf4j
@Configuration
@EnableConfigurationProperties(ConfigurationProperties.class)
class AutoConfiguration extends ImportDynamicResourcesForConfiguration{
	
	@Autowired
	ConfigurationProperties configurationProperties
	
	
	@Autowired
	ApplicationContext context

	@Bean
	EventHandler autoConfigure(){
		
		// extract the property values coming from the property file filed by the user by introspection
		
		String beanName
		String property
		String propertyValue
		
		Class propertiesClass = configurationProperties.getClass()
		Field[] fields = propertiesClass.getFields()
		for(Field field: fields){
			beanName = field.getName()
			String methodName = beanName
			methodName = methodName.substring(0,1).toUpperCase().concat(methodName.substring(1))
			methodName = "get" + methodName
			Method method = propertiesClass.getMethod(methodName, null)
			Object returnedValue = method.invoke(configurationProperties, null)
			if(returnedValue != null){
				Class propertyClass = returnedValue.getClass()
				Field[] propertyFields = propertyClass.getFields()
				for(Field propertyField: propertyFields){
					property = propertyField.getName()
					methodName = property
					methodName = methodName.substring(0,1).toUpperCase().concat(methodName.substring(1))
					methodName = "get" + methodName
					method = propertyClass.getMethod(methodName, null)
					propertyValue = (String)method.invoke(returnedValue, null)
					if(propertyValue != null){
						
						// search for an orcha configuration bean having beanName as a name
						// then set the property
						 
						//println "propertyValue = " + propertyValue
						Object bean = context.getBean(beanName)
						//println "bean = " + bean
						if(bean.output != null){
							ConfigurableProperties configurableProperties = bean.output.adapter
							MetaClass metaClass = bean.output.adapter.getMetaClass()
							if(configurableProperties.properties.contains(property)){
								//println 'setProperty'
								metaClass.setProperty(bean.output.adapter, property, propertyValue)
							}
						}
						if(bean.input != null){
							ConfigurableProperties configurableProperties = bean.input.adapter
							MetaClass metaClass = bean.input.adapter.getMetaClass()
							if(configurableProperties.properties.contains(property)){
								//println 'setProperty'
								metaClass.setProperty(bean.input.adapter, property, propertyValue)
							}
						}
					}
				}				
			}
		}
		

	}

}