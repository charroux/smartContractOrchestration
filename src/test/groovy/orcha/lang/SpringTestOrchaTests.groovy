package orcha.lang

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
//@ContextConfiguration(locations=["classpath:select best TV vendors.xml"])
@SpringBootTest(classes=[orcha.lang.OrchaM.class])
class SpringTestOrchaTests {
	
	@Test
	public void testContextLoads() throws Exception {
		assertThat(new String("essai")).isNotNull();
	}

}
