package orcha.lang.programlifecycle

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component

/**
 * Use spring batch to manage step by step the life cycle of a ComposeProgram.
 * A typical life cycle is: 
 * - check if the Compose program has been updated
 * - check if the configuration has changed
 * - generate a configuration bean
 * - compile the Compose program
 * - build the project
 * - launch the project
 * 
 * The life cycle is managed by Spring batch.
 * The Spring batch configuration is defined into SpringBatchContext.xml (src/main/resources)
 * 
 * @author Charroux
 *
 */
@Component
class ProgramLifeCycleManagement {
	
	@Autowired
	JobLauncher jobLauncher
	
	@Autowired
	ApplicationContext context
	
	ProgramLifeCycleExecution programLifeCycleExecution

	/**
	 * 
	 * @param composeProgram the program whose life cycle is managed
	 * @param jobName the name of the job has defined into the Spring batch configutation (see src/main/resources/SpringBatchContext.xml)
	 * 	 
	 */
	ProgramLifeCycleExecution execute(OrchaProgram composeProgram, String jobName){
		
		Job job = context.getBean(jobName)
		
		BuildAllAndRun programLifeCycleImplantation = context.getBean("buildAllAndRun")
		programLifeCycleImplantation.composeProgram = composeProgram

		JobExecution jobExecution = jobLauncher.run(job, new JobParameters())
		programLifeCycleExecution = new ProgramLifeCycleExecution(jobExecution: jobExecution, programLifeCycleImplantation:programLifeCycleImplantation)
		
		return programLifeCycleExecution
		
	}
	
	def stop(OrchaProgram composeProgram, String jobName){
		
		BuildAllAndRun programLifeCycleImplantation = context.getBean("buildAllAndRun")
		programLifeCycleImplantation.stopProgram()
		
		programLifeCycleExecution.stopExecution()
		
	}
}
