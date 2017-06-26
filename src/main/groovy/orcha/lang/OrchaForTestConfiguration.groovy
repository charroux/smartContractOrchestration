package orcha.lang

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource

import orcha.lang.configuration.OrchaSession

@Configuration
@ComponentScan(basePackages=["configuration","generated"])
@ImportResource([
	"test data for BenchmarkingVendors.xml",
	"test data for BenchmarkingVendorsQoS.xml"
])
class OrchaForTestConfiguration {
	
	@Bean
	OrchaSession orchaSession(){
		return new OrchaSession()
	}

}
