package orcha.lang

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import orcha.lang.compiler.visitor.OrchaCodeParserTest

@RunWith(Suite.class)
@Suite.SuiteClasses([
	OrchaCodeParserTest.class,
	SpringTestOrchaTests.class
])
class GroovyTestSuite {

}
