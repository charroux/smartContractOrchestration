package orcha.lang.contract

import orcha.lang.compiler.qualityOfService.QualityOfService
import orcha.lang.compiler.qualityOfService.QualityOfServiceImpl
import orcha.lang.contract.impl.ContractGeneratorImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ContractGenerationConfiguration {

	@Bean
	ContractGenerator contractGenerator(){
		return new ContractGeneratorImpl()
	}
	
	@Bean
	QualityOfService qualityOfService(){
		return new QualityOfServiceImpl()
	}
	
}
