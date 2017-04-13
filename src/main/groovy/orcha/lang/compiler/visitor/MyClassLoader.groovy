package orcha.lang.compiler.visitor

import groovy.lang.GroovyClassLoader;

import java.security.CodeSource;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases

class MyClassLoader extends GroovyClassLoader { 
	CodeVisitorSupport visitor 
	protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) { 
		CompilationUnit cu = super.createCompilationUnit(config, source) 
		cu.addPhaseOperation(new CustomSourceOperation(visitor: visitor), Phases.CLASS_GENERATION) 
		return cu 
	} 
}
