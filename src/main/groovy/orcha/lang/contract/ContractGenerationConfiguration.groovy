package orcha.lang.contract

import orcha.lang.contract.impl.ContractGeneratorImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ContractGenerationConfiguration {

	@Bean
	ContractGenerator contractGenerator(){
		return new ContractGeneratorImpl()
	}
	
}
