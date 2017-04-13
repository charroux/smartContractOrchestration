package orcha.langimport groovy.util.logging.Slf4j;import orcha.lang.compiler.referenceimpl.xmlgenerator.impl.ErrorUnwrapper;import orcha.lang.configuration.EventHandler;import orcha.lang.configuration.OrchaSessionimport org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.SpringApplicationimport org.springframework.boot.autoconfigure.EnableAutoConfiguration;import org.springframework.context.ApplicationContextimport org.springframework.context.annotation.AnnotationConfigApplicationContextimport org.springframework.context.annotation.Bean;import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResourceimport org.springframework.core.env.AbstractEnvironment
/** *  * Orcha xml spring context are loaded dynamically from the auo generated class ImportDynamicResources *  * @author Ben C. * */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages=["configuration","generated"])@Slf4j
class OrchaSpringIntegrationLauncher{	// extends ImportDynamicResources{		@Bean	OrchaSession orchaSession(){		return new OrchaSession()	}			public static void main(String[] args) {		try{			System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "default");	// restoring the String http default port to 8080 (application.properties)			SpringApplication application = new SpringApplication(OrchaSpringIntegrationLauncher.class)			application.run(args)		} catch(org.springframework.beans.factory.BeanCreationException e){			log.error("Configuration error into the configuration file: " + e.getRootCause(), e)		} catch(org.springframework.integration.transformer.MessageTransformationException e){			log.error(e.getLocalizedMessage(), e)		}			}

}
