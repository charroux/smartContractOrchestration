package orcha.lang

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import orcha.lang.compiler.visitor.OrchaCodeParserTest
import orcha.lang.compiler.referenceimpl.CompileServiceWithSpringIntegrationTest

@RunWith(Suite.class)
@Suite.SuiteClasses([
	OrchaCodeParserTest.class,
	SpringTestOrchaTests.class,
	CompileServiceWithSpringIntegrationTest.class
])
class GroovyTestSuite {

}
