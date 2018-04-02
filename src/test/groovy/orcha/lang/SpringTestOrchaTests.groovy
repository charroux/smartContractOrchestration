package orcha.lang

import org.junit.Before
import org.junit.Test;
import org.junit.Assert
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import groovy.util.logging.Slf4j
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaCodeVisitor
import service.order.Order
import service.order.Product
import service.order.SpecificOrder
import service.order.VendorOrderConverter

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import javax.validation.constraints.AssertTrue

@RunWith(SpringRunner.class)
@SpringBootTest(classes=[orcha.lang.ImportDynamicResourcesForTestConfiguration.class])
class SpringTestOrchaTests {	
	
	@Test
	void test(){
		Thread.sleep(3000)	// !!!!! to wait for file writing
	}
	
}
