package orcha.lang.compiler.visitor.impl

import java.lang.reflect.Method
import java.util.List;
import java.util.Map

import orcha.lang.compiler.OrchaCompilationException
import orcha.lang.compiler.OrchaConfigurationException
import orcha.lang.compiler.OrchaMetadata
import orcha.lang.compiler.Instruction
import orcha.lang.compiler.InstructionNode
import orcha.lang.compiler.qualityOfService.QualityOfServicesOptions;
import orcha.lang.compiler.referenceimpl.ExpressionParser;
import orcha.lang.compiler.visitor.OrchaCodeParser
import orcha.lang.compiler.visitor.OrchaComplianceException
import orcha.lang.compiler.visitor.MyClassLoader
import orcha.lang.configuration.Application

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit;
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Service

import groovy.util.logging.Slf4j

/**
 * 
 * 
 * @author Ben C.
 *
 */
@Slf4j
class OrchaCodeVisitor extends OrchaCodeParser{
	
	@Autowired
	ApplicationContext context
	
	@Autowired
	ExpressionParser expressionParser
	
	//def aggregators = []
	File orchaFile
	
	private def instructions = []
	
	OrchaMetadata orchaMetadata = new OrchaMetadata()

	public OrchaMetadata getOrchaMetadata() {
		return orchaMetadata;
	}
	
	private List<InstructionNode> graphOfInstructions = []
	
	// def graphOfInstructions = []
	
	//def predecedings = [:]

	/**
	 * @return the adjacency graph containing all Orcha  instructions.
 	 */
	List<InstructionNode> findAllNodes(){
		return graphOfInstructions
	}
	
	InstructionNode findAdjacentNode(InstructionNode node){
		return graphOfInstructions.find { it.instruction == node.instruction }
	}
	
	/**
	 * @return all adjacent instructions of the given node.
	 */
	InstructionNode findNextRawNode(InstructionNode node){
		if(node.next == null){
			return null
		}
		return graphOfInstructions.find { it.instruction == node.next.instruction }
	}
	
	List<InstructionNode> findNextNode(InstructionNode node){
		
		def nodes = []
		
		//InstructionNode n
		
		if(node.instruction.instruction=="receive" && node.next!=null && node.next.instruction.instruction=="receive"){
			node = graphOfInstructions.find { it.instruction == node.instruction }
			node = graphOfInstructions.find { it.instruction == node.next.instruction }
			nodes.add(  node )
		} else if(node.instruction.instruction=="when" && node.next!=null && node.next.instruction.instruction=="when"){
			node = graphOfInstructions.find { it.instruction==node.instruction}
			node = graphOfInstructions.find { it.instruction == node.next.instruction }
			nodes.add(  node )
		} else if(node.instruction.instruction=="when" && node.next==null){
			node = graphOfInstructions.find { it.instruction == node.instruction }
			node = graphOfInstructions.find { it.instruction == node.next.instruction }
			nodes.add(  node )
		} else {
			node = this.findNextRawNode(node)
			if(node != null){
				if(node.instruction.instruction=="when" && node.instruction.variable==null){
					node = node.next
					while(node != null){
						nodes.add(node)
						node = node.next
					}
				} else {
					nodes.add( node )
				}				
			}			
		}
			
		return nodes 
		
	}
	
	/*List<InstructionNode> findNextNode(InstructionNode node){
		InstructionNode next
		def nodes = []
		if(node.next==null){
			return nodes
		}
		if(node.instruction.instruction=="receive" && node.next.instruction.instruction=="receive"){
			while(node.next!=null && node.next.instruction.instruction=="receive"){
				next = graphOfInstructions.find { it.instruction == node.next.instruction }
				nodes.add(next)
				node = node.next
			}
		} else if(node.instruction.instruction=="when" && node.next.instruction.instruction=="when"){
			node = node.next
			while(node != null){
				next = graphOfInstructions.find { it.instruction == node.instruction }
				nodes.add(next)
				node = node.next
			}
			while(node.next!=null && node.next.instruction.instruction=="when"){
				next = graphOfInstructions.find { it.instruction == node.next.instruction }
				nodes.add(next)
				node = node.next
			}
		}else {
			next = graphOfInstructions.find { it.instruction == node.next.instruction }
			nodes.add(next)
		}
		return nodes
	}*/

	InstructionNode findNextRawNode(InstructionNode node, List<InstructionNode> instructionsToExclude){
		return graphOfInstructions.find { instructionsToExclude.contains(it)== false && it.instruction==node.next.instruction }
	}
	
	InstructionNode findNextRawNode(InstructionNode node, InstructionNode nodeToExclude){
		return graphOfInstructions.find{ it!=nodeToExclude && it.instruction.is(node.next.instruction) }
	}
	
	List<InstructionNode> findAllReceiveNodes(){
		return graphOfInstructions.findAll { it.instruction.instruction == "receive"}
	}
	
	List<InstructionNode> findAllSendNodes(){
		return graphOfInstructions.findAll { it.instruction.instruction == "send"}
	}
	
	List<InstructionNode> findAllComputeNodes(){
		return graphOfInstructions.findAll { it.instruction.instruction == "compute"}
	}
	
	List<InstructionNode> findAllWhenNodes(){
		return graphOfInstructions.findAll { it.instruction.instruction == "when"}
	}
	
	List<InstructionNode> findAllWhenNodesWithTheSameApplicationsInExpression(){
		return graphOfInstructions.findAll { it.instruction.instruction=="when" && it.next!=null && it.next.instruction.instruction=="when" }
	}
	
	List<InstructionNode> findAllWhenNodesWithDifferentApplicationsInExpression(){
		return graphOfInstructions.findAll { it.instruction.instruction=="when" && it.next!=null && it.next.instruction.instruction!="when" }
	}
	
	List<InstructionNode> findAllWhenNodesWithManyApplicationsInExpression(){		
		return graphOfInstructions.findAll { 
			it.instruction.instruction=="when" &&
			(expressionParser.getApplicationsNamesInExpression(it.instruction.variable, graphOfInstructions).size() > 1)
		}
	}
	
/*	boolean isDifferentNodesWithSameApplicationsInExpression(InstructionNode node1, InstructionNode node2){
		
		if(node1.instruction.instruction!="when" || node2.instruction.instruction!="when"){
			return false
		}
		
		if(node1 == node2){
			return false
		}
		
		String orchaExpression1 = node1.instruction.variable		
		List<Application> apps1 = expressionParser.getApplicationsInExpression(orchaExpression1, graphOfInstructions)
		
		String orchaExpression2 = node2.instruction.variable		
		List<Application> apps2 = expressionParser.getApplicationsInExpression(orchaExpression2, graphOfInstructions)
		
		return Arrays.equals(apps1.toArray(), apps2.toArray())
		
	}*/
	
/*	List<InstructionNode> findAllWhenNodesWithFailsAndTerminatesInExpression(){
		def whenNodes = []
		List<InstructionNode> nodes = this.findAllWhenNodesWithTheSameApplicationsInExpression()
		nodes.each { node ->
			node = node.next
			boolean isRightNode = false
			while(node != null){
				List<InstructionNode> precedingNodes = this.findAllPrecedingNodes(node)
				if(precedingNodes.size() == 1){
					isRightNode = expressionParser.isComputeFailsInExpression(precedingNodes.getAt(0), node.instruction.variable)
				} else {
					isRightNode = expressionParser.isComputeTerminatesInExpression(precedingNodes.getAt(0), node.instruction.variable)
				}
				node = node.next
			}	
			if(isRightNode == true){
				whenNodes.add(node)
			}			
		}
		return whenNodes
	}*/
	
	List<InstructionNode> findAllReceiveNodesWithTheSameEvent(){
		return graphOfInstructions.findAll { it.instruction.instruction=="receive" && it.next!=null && it.next.instruction.instruction=="receive" }
	}
	
	/**
	 * Replace the artificial when nodes with the same applications by the related compute instruction 
	 */
	List<InstructionNode> findAllPrecedingNodes(InstructionNode node){
		
		List<InstructionNode> nodes = graphOfInstructions.findAll{ it.next!=null && it.next.instruction == node.instruction }
		
		InstructionNode previous
		InstructionNode beforePrevious 
		
		for(int index=0; index<nodes.size(); index++){
			
			previous = nodes.getAt(index)
			
			if(previous.instruction.instruction=="when" && node.instruction.instruction=="when"){
				
				List<Application> applications = expressionParser.getApplicationsInExpression(node.instruction.variable, graphOfInstructions)
				
				for(int i=0; i<applications.size(); i++){		// starting from the applications, look for the related compute (having program1 as variable)
						
					beforePrevious = graphOfInstructions.find{ it.instruction.instruction=="compute" && it.instruction.variable==applications[i].name }
				
					if(beforePrevious != null){
						nodes.remove(index)
						nodes.add(index, beforePrevious)						
					}
				}
			}
		}
		return nodes
	}
	
	List<InstructionNode> findAllRawPrecedingNodes(InstructionNode node){
	
		//println "-->>>" + node
			
		List<InstructionNode> nodes = graphOfInstructions.findAll{ it.next!=null && it.next.instruction == node.instruction }
		
		InstructionNode beforePrevious
		
		if(nodes.size() == 0){
			
			//if(node.instruction.instruction=="when" && node.next!=null && node.next.instruction.instruction=="when"){
			if(node.instruction.instruction == "when"){
				
				String orchaExpression = node.instruction.variable
				if(orchaExpression == null){
					orchaExpression = node.next.instruction.variable
				}
				
				List<Application> applications = expressionParser.getApplicationsInExpression(orchaExpression, graphOfInstructions)
				
				for(int i=0; i<applications.size(); i++){		// starting from the applications, look for the related compute (having program1 as variable)
						
					beforePrevious = graphOfInstructions.find{ it.instruction.instruction=="compute" && it.instruction.variable==applications[i].name }
				
					if(beforePrevious != null){
						if(nodes.contains(beforePrevious) == false){
							nodes.add(beforePrevious)
						}						
					}
				}
			}
		} else {
			InstructionNode previous
			
			def previousNodes = [:]
			
			for(int index=0; index<nodes.size(); index++){
				
				previous = nodes.getAt(index)
				
				if(previous.instruction.instruction=="when" && node.instruction.instruction=="when"){
					
					List<Application> applications = expressionParser.getApplicationsInExpression(node.instruction.variable, graphOfInstructions)
					
					for(int i=0; i<applications.size(); i++){		// starting from the applications, look for the related compute (having program1 as variable)
							
						beforePrevious = graphOfInstructions.find{ it.instruction.instruction=="compute" && it.instruction.variable==applications[i].name }
					
						if(beforePrevious != null){
							previousNodes.put(index+i, beforePrevious)
						}
					}
				}
			}
			
			previousNodes.each { key, value ->
				if(nodes.contains(value) == false){
					nodes.add(key, value)
				}
			}
			
		}
		
		return nodes
		
	}
	
	Map<Class, List<InstructionNode>> findAllComputeNodesWithoutAdapterByConfigurationClass(){
		
		def beansByConfigurationClass = [:]
		
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
		
		List<InstructionNode> computeNodes = this.findAllComputeNodes()
		
		for (BeanDefinition beanDef : provider.findCandidateComponents("configuration.*")) {
			
			Class<?> configurationClass = Class.forName(beanDef.getBeanClassName());
			Configuration findable = configurationClass.getAnnotation(Configuration.class);
			
			Method[] methods = configurationClass.getMethods();
			
			def beans = []
			
			for(Method method: methods){
				InstructionNode instruction = computeNodes.find{ it.instruction.springBean.input!=null && it.instruction.springBean.input.adapter==null && it.instruction.springBean.output!=null && it.instruction.springBean.output.adapter==null && it.instruction.springBean.name.equals(method.getName())}
				if( instruction != null){
					beans.add(instruction)
				}
			}
			
			if(beans.empty == false){
				beansByConfigurationClass[configurationClass] = beans
			}
			
		}
		
		return beansByConfigurationClass
	}
	
	Map<Class, List<InstructionNode>> findAllNodesWithoutAdapterByConfigurationClass(){
		
		def beansByConfigurationClass = [:]
		
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
		
		List<InstructionNode> computeNodes = this.findAllComputeNodes()
		
		List<InstructionNode> receiveNodes = this.findAllReceiveNodes();
		
		List<InstructionNode> sendNodes = this.findAllSendNodes();
		
		for (BeanDefinition beanDef : provider.findCandidateComponents("configuration.*")) {
			
			Class<?> configurationClass = Class.forName(beanDef.getBeanClassName());
			Configuration findable = configurationClass.getAnnotation(Configuration.class);
			
			Method[] methods = configurationClass.getMethods();
			
			def beans = []
			
			for(Method method: methods){
				InstructionNode instruction = computeNodes.find{ it.instruction.springBean.input!=null && it.instruction.springBean.input.adapter==null && it.instruction.springBean.output!=null && it.instruction.springBean.output.adapter==null && it.instruction.springBean.name.equals(method.getName())}
				if( instruction != null){
					beans.add(instruction)
				}
			}
			
			for(Method method: methods){
				InstructionNode instruction = receiveNodes.find{ it.instruction.springBean.input!=null && it.instruction.springBean.input.adapter==null && it.instruction.springBean.name.equals(method.getName())}
				if( instruction != null){
					beans.add(instruction)
				}
			}
			
			for(Method method: methods){
				InstructionNode instruction = sendNodes.find{ it.instruction.springBean.output!=null && it.instruction.springBean.output.adapter==null && it.instruction.springBean.name.equals(method.getName())}
				if( instruction != null){
					beans.add(instruction)
				}
			}
			
			if(beans.empty == false){
				beansByConfigurationClass[configurationClass] = beans
			}
			
		}
		
		return beansByConfigurationClass
	}
	
//	boolean isFailsAndTerminatesInExpression(InstructionNode node){
//		
//	}
	
	void depthTraversal(def object, String methodName, InstructionNode node, OrchaCodeParser orchaCodeParser, List<InstructionNode> nodesAlreadyDone){
		
		if(nodesAlreadyDone.contains(node) == false){
	
			if(node.instruction.instruction == "receive"){
				nodesAlreadyDone.add(node)
			}
			
			if(node.next == null){
				return
			}
			
			while(node.next!=null && node.next.instruction.instruction == "receive"){
				
				// search in the graphOfInstructions the instructionNode having the same instruction than node.next
				// excluding node.next itself
				//InstructionNode nextNode = graphOfInstructions.find{ it!=node.next && it.instruction.is(node.next.instruction) }
				InstructionNode nextNode = orchaCodeParser.findNextRawNode(node, node.next)
				if(nextNode != null){
					object."$methodName"(nextNode, node)
					depthTraversal(object, methodName, nextNode, orchaCodeParser, nodesAlreadyDone);
				}
				node = node.next
			}
			
			if(node.next != null){
				
				// search in the graphOfInstructions the instructionNode having the same instruction than node.next
				// excluding node.next itself
				//InstructionNode nextNode = graphOfInstructions.find{ it!=node.next && it.instruction.is(node.next.instruction) }
				InstructionNode nextNode = orchaCodeParser.findNextRawNode(node, node.next)
				if(nextNode != null){
					object."$methodName"(nextNode, node)
					depthTraversal(object, methodName, nextNode, orchaCodeParser, nodesAlreadyDone);
				}
			}
		}
	}

	
	
	/**
	 * 
	 * @return a map, an instruction as a key, a list on preceding instructions
	 */
	/*Map<InstructionNode, List<InstructionNode>> getPredecedingInstructions(){
		if(predecedings.size() == 0){
			this.graphOfInstructions()
		}
		return predecedings
	}*/
	
	/**
	 * 
	 * 
     * @return an adjacency graph of instructions
	 */
	private List<InstructionNode> getGraphOfInstructions(){
		
		if(graphOfInstructions.empty){
			
			log.info 'Syntax analysis of the the Orcha program'
		
			checkOrchaCompliance()
			
			Instruction instruction
			
			Iterator<Instruction> listOfInsctructions = instructions.iterator()
			
			while(listOfInsctructions.hasNext()){
				instruction = listOfInsctructions.next()
				InstructionNode instructionNode = new InstructionNode(instruction: instruction, next: null)
				graphOfInstructions.add(instructionNode)
			}
			
			Iterator<InstructionNode> listOfNodes = graphOfInstructions.iterator()
			
			int lineNumber = 1
			InstructionNode instructionNode
			
			while(listOfNodes.hasNext()){
				
				instructionNode = listOfNodes.next()
				
				if(instructionNode.instruction.instruction == "receive"){ // receive event from inputFile
	
					def variable = instructionNode.instruction.variable
					
					if(variable != null){								// look for an associated with: compute program1 with event.value
						int i=lineNumber
						while(i<graphOfInstructions.size() && graphOfInstructions.get(i).instruction.containsWith(variable)==false){
							i++
						}
						
						if(i<graphOfInstructions.size()){
							instructionNode.next = new InstructionNode(instruction: graphOfInstructions.get(i).instruction, next: null)
							/*def list = []
							list.add(instructionNode)
							predecedings.put(graphOfInstructions.get(i), list);*/
						}
					}
		
				} else if(instructionNode.instruction.instruction == "when"){	// when "program1 terminates"
				
					Application[] applications = expressionParser.getApplicationsInExpression(instructionNode.instruction.variable, graphOfInstructions) // applications containing program1
					
					if(applications.length == 0){
						Throwable error = new OrchaConfigurationException("Unable to find such application " + instructionNode.instruction.variable + " in a compute instruction")
						log.error("Unable to find such application " + "(instructionNode.instruction.variable) in a compute instruction", error)
						throw error
					}
									 
					for(int i=0; i<applications.length; i++){		// starting from the applications, look for the related compute (having program1 as variable)
						
						InstructionNode computeNode = graphOfInstructions.find { it.instruction.instruction=="compute" && it.instruction.variable==applications[i].name }
						
						String orchaExpression = instructionNode.instruction.variable
						
						if(expressionParser.isComputeFailsInExpression(computeNode, orchaExpression) == false){
							computeNode.next = new InstructionNode(instruction: instructionNode.instruction, next: null)
						}
						
						
/*						int j=0
						while(j<graphOfInstructions.size() && (graphOfInstructions.get(j).instruction.instruction!="compute" || graphOfInstructions.get(j).instruction.variable!=applications[i].name)){
							j++
						}
							
						if(j<graphOfInstructions.size()){
							graphOfInstructions.get(j).next = new InstructionNode(instruction: instructionNode.instruction, next: null)
						} else {
						}
*/						
					}
					
					InstructionNode instructionAfterWhen = listOfNodes.next()
					instructionNode.next = new InstructionNode(instruction: instructionAfterWhen.instruction, next: null)
				}
				
				lineNumber++
				
			}
			
			def alreadyHandledNodes = []
			
			def allReceiveNodes = graphOfInstructions.findAll { it.instruction.instruction == "receive" }

			// select the receive instruction nodes having the same event source: receive event from eventSource
			// then put all those nodes after a generic receive node (for the event source): they are adjacent (leads to 2 branches)
		 
			for(InstructionNode receiveNode: allReceiveNodes){
				
				def receivesWithSameEventHandler = graphOfInstructions.findAll {it.instruction!=null && it.instruction.springBean==receiveNode.instruction.springBean}
				
				boolean  alreadyHandled = false
				
				int i=0
				
				while(i<alreadyHandledNodes.size() && alreadyHandled==false){
					if(receivesWithSameEventHandler.find { it.instruction == alreadyHandledNodes.getAt(i).instruction }!=null){
						alreadyHandled = true
					}
					i++
				}
				
				if(receivesWithSameEventHandler.size()>1 && alreadyHandled==false){
				
					alreadyHandledNodes.addAll(receivesWithSameEventHandler)
					
					Instruction genericReceiveInstruction = new Instruction(instruction: "receive", variable: null, variableProperty: null, springBean: receivesWithSameEventHandler.getAt(0).instruction.springBean, withs: null, condition: null)
					
					InstructionNode newRootNode = new InstructionNode(instruction: genericReceiveInstruction, next: null)
					
					graphOfInstructions.add(0, newRootNode)
					
					for(i=0; i<receivesWithSameEventHandler.size(); i++){
						InstructionNode newNode = new InstructionNode(instruction: receivesWithSameEventHandler.getAt(i).instruction, next: null)
						newRootNode.next = newNode
						newRootNode = newNode
					}
					
				}
			}
			
			alreadyHandledNodes = []
			
			def allWhenNodes = graphOfInstructions.findAll { it.instruction.instruction == "when" }

			// select all the when instructions nodes having the same application in their expression
			// then put all those nodes after a generic send node: they are adjacent (leads to 2 branches)
		 
			for(InstructionNode whenNode: allWhenNodes){
				
				Application[] applications = expressionParser.getApplicationsInExpression(whenNode.instruction.variable, graphOfInstructions)
				
				def whenNodesWithSameApplications = []
				def whenNodesWithFailExpression = []
				
				int i=0;
			
				InstructionNode node
				Application[] applicationsArray
				while(i<allWhenNodes.size()){
					node = allWhenNodes.getAt(i)
					if(node!=whenNode && alreadyHandledNodes.contains(node)==false){
						applicationsArray = expressionParser.getApplicationsInExpression(node.instruction.variable, graphOfInstructions)
						if(Arrays.equals(applications, applicationsArray)){					
							//def applicationNames = expressionParser.getApplicationsNamesInExpression(node.instruction.variable, graphOfInstructions)
							//applicationNames = applicationNames.findAll{ expressionParser.isComputeFailsInExpression(it, node.instruction.variable) == false}
							//if(applications.size() == applicationNames.size()){
								whenNodesWithSameApplications.add(node)
							//}							
						}
						String[] applicationNames = expressionParser.getApplicationsNamesInExpression(node.instruction.variable, graphOfInstructions)
						applicationNames.each { appliName ->
							InstructionNode n = graphOfInstructions.find { it.instruction.instruction=="when" && it.instruction.variable!=null && expressionParser.isComputeFailsInExpression(appliName, it.instruction.variable)}
							if(n != null){
								whenNodesWithFailExpression.add(n)
							}						
						}
					} 
					i++
				}
				
				InstructionNode newRootNode
				
				if(whenNodesWithSameApplications.size() > 0){
					alreadyHandledNodes.add(whenNode)
					alreadyHandledNodes.addAll(whenNodesWithSameApplications)
					
					//Instruction genericReceiveInstruction = new Instruction(instruction: "when", variable: whenNode.instruction.variable, variableProperty: null, springBean: null, withs: null, condition: null)
					Instruction genericReceiveInstruction = new Instruction(instruction: "when", variable: null, variableProperty: null, springBean: null, withs: null, condition: null)
					
					newRootNode = new InstructionNode(instruction: genericReceiveInstruction, next: null)

					List<InstructionNode> precedingNodes = this.findAllPrecedingNodes(whenNode)
					precedingNodes.each { precedingNode ->
						precedingNode.next = newRootNode
					}
					
					int index = 0;
					while(graphOfInstructions.getAt(index)!=whenNode){
						index++
					}
					
					graphOfInstructions.add(index, newRootNode)
					
					InstructionNode newNode = new InstructionNode(instruction: whenNode.instruction, next: null)
					
					
					
					if(newNode.options == null){							
						newNode.options = new QualityOfServicesOptions()
					}
					
					newNode.options.severalWhenWithSameApplicationsInExpression = true
					
					newRootNode.next = newNode
					newRootNode = newNode
					
					whenNodesWithSameApplications.each {
						newNode = new InstructionNode(instruction: it.instruction, next: null)
	
						if(newNode.options == null){
							newNode.options = new QualityOfServicesOptions()
						}
						
						newNode.options.severalWhenWithSameApplicationsInExpression = true
	
						newRootNode.next = newNode
						newRootNode = newNode
					}					
				}	
				
				if(whenNodesWithFailExpression.size() > 0){
					alreadyHandledNodes.add(whenNode)
					alreadyHandledNodes.addAll(whenNodesWithFailExpression)
					
					if(whenNodesWithSameApplications.size() == 0){
						Instruction genericReceiveInstruction = new Instruction(instruction: "when", variable: null, variableProperty: null, springBean: null, withs: null, condition: null)
						newRootNode = new InstructionNode(instruction: genericReceiveInstruction, next: null)
					
						List<InstructionNode> precedingNodes = this.findAllPrecedingNodes(whenNode)
						precedingNodes.each { precedingNode ->
							precedingNode.next = newRootNode
						}
						
						int index = 0;
						while(graphOfInstructions.getAt(index)!=whenNode){
							index++
						}
						
						graphOfInstructions.add(index, newRootNode)
	
					}
									
					InstructionNode newNode = new InstructionNode(instruction: whenNode.instruction, next: null)
					
					if(newNode.options == null){				
						newNode.options = new QualityOfServicesOptions()
					}
					
					if(expressionParser.isFailExpression(newNode, graphOfInstructions) == true){
						newNode.options.failTest = true
					} else {
						newNode.options.failTest = false
					}
					
					
					newRootNode.next = newNode
					newRootNode = newNode
					
					whenNodesWithFailExpression.each {
						newNode = new InstructionNode(instruction: it.instruction, next: null)
						if(newNode.options == null){							// a receive node
							newNode.options = new QualityOfServicesOptions()
						}
						if(expressionParser.isFailExpression(newNode, graphOfInstructions) == true){
							newNode.options.failTest = true
						} else {
							newNode.options.failTest = false
						}
						newRootNode.next = newNode
						newRootNode = newNode
					}
				}
			}
			
			log.info 'Syntax analysis of the the Orcha program complete successfully'
			
		}
		
		return graphOfInstructions
	}
		
	@Override
	public void parseSourceFile(File orchaFile) throws OrchaCompilationException, OrchaConfigurationException {
		
		this.orchaFile = orchaFile
		
		try{
			
			def myCL = new MyClassLoader(visitor: this)
			
			//try{
			//	def script = myCL.parseClass(new GroovyCodeSource(orchaFile))
			/*} catch(NoSuchBeanDefinitionException e){
				println 'ooooooooooooooookkkkkkkkkkkkkkkkk'
				String message = "Orcha configuration error while parsing the orcha file (" + orchaFile.getAbsolutePath() + "): no definition for "  + e.getBeanName() + ". Please define it as a bean in an Oche configuration class."
				log.error "Orcha configuration error while parsing the orcha file (" + orchaFile.getAbsolutePath() + "): no definition for "  + e.getBeanName() + ". Please define it as a bean in an Oche configuration class."
				throw new OrchaConfigurationException(message)					
			}*/
			
	
			try{
	
				def script = myCL.parseClass(new GroovyCodeSource(orchaFile))
				
				this.getGraphOfInstructions()
				
			} catch(org.codehaus.groovy.control.MultipleCompilationErrorsException e){
				Iterator errors = e.getErrorCollector().getErrors().iterator()
				while(errors.hasNext()){
					throw errors.next().getCause()
				}
			}
		}catch(org.springframework.beans.factory.NoSuchBeanDefinitionException e){
			String message = "Orcha configuration error while parsing the orcha file (" + orchaFile.getAbsolutePath() + "): no definition for "  + e.getBeanName() + ". Please define it as a bean in an Oche configuration class."
			log.error "Orcha configuration error while parsing the orcha file (" + orchaFile.getAbsolutePath() + "): no definition for "  + e.getBeanName() + ". Please define it as a bean in an Oche configuration class."
			throw new OrchaConfigurationException(message)
		}catch(Exception e){
			log.error e.getMessage()
			throw e
		}
	}

	void visitConstantExpression(ConstantExpression expression){
		log.debug "begin"
	}	
	
	/**
	 * méthode qui appelle la classe de création de fichier xml
	 */
	void visitMethodCallExpression(MethodCallExpression call){
			
		log.debug "begin"
		
		def method
		
		def instruction
		
		Expression expression = call.getObjectExpression()
		
		if(expression instanceof MethodCallExpression){
			MethodCallExpression methodExpression = (MethodCallExpression)expression
			expression = methodExpression.getMethod()
			if(expression instanceof ConstantExpression){
				ConstantExpression constantExpression = (ConstantExpression)expression
				method = constantExpression.getText() 
				log.debug method	+ " from ConstantExpression"	// from
			}
			expression = methodExpression.getObjectExpression()
			if(expression instanceof MethodCallExpression){
				MethodCallExpression methodExp = (MethodCallExpression)expression
				expression = methodExp.getArguments()
				if(expression instanceof ArgumentListExpression){
					ArgumentListExpression argumentListExpression = (ArgumentListExpression)expression
					Iterator<Expression> args = argumentListExpression.getExpressions().iterator()
					def i = 0
					while(args.hasNext()){
						expression = args.next()
						if(expression instanceof VariableExpression){
							VariableExpression variableExpression = (VariableExpression)expression
							def arg = variableExpression.getText()	// incomingMessageEvent
							log.debug arg + " incomingMessageEvent VariableArgumentExpression " + i
							if(method == 'from'){
								instruction = new Instruction(variable: arg)
							}
							i++
						}
					}
				}
				expression = methodExp.getMethod()
				if(expression instanceof ConstantExpression){
					ConstantExpression constantExpression = (ConstantExpression)expression
					method = constantExpression.getText()
					log.debug method	+ " receive ConstantExpression"	// receive
					if(method == 'receive'){
						instruction.instruction = 'receive'
					}
				}
			}
						
			expression = methodExpression.getArguments()
			if(expression instanceof ArgumentListExpression){
				ArgumentListExpression argumentListExpression = (ArgumentListExpression)expression
				Iterator<Expression> args = argumentListExpression.getExpressions().iterator()
				def i = 0
				while(args.hasNext()){
					expression = args.next()
					if(expression instanceof VariableExpression){
						VariableExpression variableExpression = (VariableExpression)expression
						def arg = variableExpression.getText()	// event, code1, composeEvents
						log.debug arg + " composeEvents VariableArgumentExpression " + i
						i++
						if(instruction!=null && instruction.instruction=='receive'){
							instruction.springBean = context.getBean(arg)
						} else if(method == 'compute'){
							instruction = new Instruction(instruction:'compute', variable: arg, springBean: context.getBean(arg))		// arg instanceof Application
						} else if(method == 'receive'){
							instruction = new Instruction(instruction:'receive', variable: arg)
						} else if(method == 'send'){		// added
							instruction = new Instruction(instruction:'send', variable: arg)
						}
					}
				}
				if(expression instanceof PropertyExpression){
					PropertyExpression propertyExpression = (PropertyExpression)expression
					expression = propertyExpression.getObjectExpression()
					if(expression instanceof VariableExpression){
						VariableExpression variableExpression = (VariableExpression)expression
						def ar = variableExpression.getText()	// code1
						if(method == 'send'){
							instruction = new Instruction(instruction:'send', variable: ar)
						}
						log.debug ar  + " PropertyVariableExpression"
					}
					expression = propertyExpression.getProperty()
					if(expression instanceof ConstantExpression){
						ConstantExpression constantExpression = (ConstantExpression)expression
						def value = constantExpression.getText()	// code1.result
						if(method == 'send'){
							instruction.variableProperty = value							
						}
						log.debug value + " PropertyValueExpression"
					}
				}
			}
		}
		
		expression = call.getMethod()
		if(expression instanceof ConstantExpression){
			ConstantExpression constantExpression = (ConstantExpression)expression
			method = constantExpression.getText()
			log.debug method	+ " with MethodExpression" //runScript, from, with
		}
		
		expression = call.getArguments()
		if(expression instanceof ArgumentListExpression){
			ArgumentListExpression argumentListExpression = (ArgumentListExpression)expression
			Iterator<Expression> args = argumentListExpression.getExpressions().iterator()
			def i = 0
			while(args.hasNext()){
				expression = args.next()
				if(expression instanceof VariableExpression){
					VariableExpression variableExpression = (VariableExpression)expression
					def arg = variableExpression.getText()	// args, input, database
					log.debug arg  + " VariableArgumentExpression " + i
					i++
					if(method == 'from'){
						instruction.springBean = context.getBean(arg)		// arg instanceof EventHandler
						//instructions.add(context.getBean(arg))		// arg instanceof EventHandler
					} else if(method == 'to'){
						instruction.springBean = context.getBean(arg)		// arg instanceof EventHandler
						//instructions.add(context.getBean(arg))		// arg instanceof EventHandler
					} else if(method == 'with'){
						def with = new Instruction.With()
						with.with = arg
						instruction.withs.add(with)
					} else if(method == 'domain'){
						this.orchaMetadata.domain = arg	
					}
				}
				if(expression instanceof PropertyExpression){
					PropertyExpression propertyExpression = (PropertyExpression)expression
					expression = propertyExpression.getObjectExpression()
					
					def with = new Instruction.With()
					
					if(expression instanceof VariableExpression){
						VariableExpression variableExpression = (VariableExpression)expression
						def prop = variableExpression.getText()		// event
						log.debug prop + " PropertyVariableExpression"
						if(method == 'with'){
							with.with = prop  
						}
					}
					expression = propertyExpression.getProperty()
					if(expression instanceof ConstantExpression){
						ConstantExpression constantExpression = (ConstantExpression)expression
						def value = constantExpression.getText() // event.value
						log.debug value + " PropertyValueExpression"
						if(method == 'with'){
							//instruction.withProperty = value
							with.withProperty = value
							instruction.withs.add(with)
						}
					}
				} else if(expression instanceof ConstantExpression){
					ConstantExpression constantExpression = (ConstantExpression)expression
					def m = constantExpression.getText()
					log.debug m	+ " incomingMessageEvent.state=INCOMING_MESSAGE ConstantExpression" // receive event with constantExpression
					if(method == 'condition'){
						instruction.condition = m
					} else if(method == 'description'){
						this.orchaMetadata.description = m
					} else if(method == 'title'){
						this.orchaMetadata.title = m
					} else if(method == 'author'){
						this.orchaMetadata.author = m
					} else if(method == 'version'){
						this.orchaMetadata.version = m
					}
				}
			}
		}
		
		if(method=='compute' || method=='with' || method=='condition' ||method=='from' || method=='to'){
			instructions.add(instruction)
			/*if(aggregateInstruction == true){
				int index = aggregators.size()-1
				aggregators.get(index).nextInstruction = instruction
				aggregateInstruction = false
			}*/
		}
		
		if(method=="when" && expression instanceof ConstantExpression){
			ConstantExpression constantExpression = (ConstantExpression)expression
			def value = constantExpression.getText() // event.value
			
			instruction = new Instruction(instruction:'when', variable: value)
			instructions.add(instruction)
			
			log.debug value + " PropertyValueExpression"
			
			/*def applications = this.getApplicationsInExpression(value)

			def aggregator
			aggregator = new Aggregator(releaseExpression: value, applications: applications)
			aggregators.add(aggregator)*/
			//aggregateInstruction = true
		} else {
			//aggregateInstruction = false
		}
		
		log.debug "end"
		
	}
	
	private void checkOrchaCompliance(){
		if(orchaMetadata.getTitle() == null){
			throw new OrchaComplianceException("Orcha metadata title is missing in: " + orchaFile.getAbsolutePath())
		}
	}
	
	protected SourceUnit getSourceUnit() {
		return source;
	}



}
