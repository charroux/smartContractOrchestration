package orcha.lang

import org.junit.Before
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import groovy.util.logging.Slf4j
import service.order.Order
import service.order.Product
import service.order.SpecificOrder
import service.order.VendorOrderConverter

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
//@SpringBootTest(classes=[orcha.lang.OrchaForTestConfiguration.class,  orcha.lang.AutoConfiguration.class])
class SpringTestOrchaTests {
	
/*	@Bean
	VendorOrderConverter vendorOrderConverter(){
		return new VendorOrderConverter()
	}*/
	
	/*@SpyBean
	@Qualifier("vendorOrderConverter")
	VendorOrderConverter vendorOrderConverter
	
	@Bean
	VendorOrderConverter vendorOrderConverter(){
		vendorOrderConverter
	}
	
	@Before
	void init(){
		VendorOrderConverter vendorOrderConverter  = spy(VendorOrderConverter.class)
		Product p = new Product(specification: "TV")
		Order order = new Order(number: 1, product: p);
		when(vendorOrderConverter.convert(order)).thenReturn( new SpecificOrder(number: 1111, product: "azerty") );
	}*/
	
	
	@Test
	void testContextLoads() throws Exception {
		assertThat(new String("essai")).isNotNull();
		//Thread.sleep(20000);
	}

}
