package orcha.lang
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages=["configuration","generated"])
class OrchaSpringIntegrationLauncher{ // extends ImportDynamicResourcesForConfiguration{

}