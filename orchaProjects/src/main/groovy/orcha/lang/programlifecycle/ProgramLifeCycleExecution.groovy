package orcha.lang.programlifecycle

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution;

class ProgramLifeCycleExecution {
	
	JobExecution jobExecution
	BuildAllAndRun programLifeCycleImplantation
	
	/**
	 * Accessor for the step executions.
	 *
	 * @return the step executions that were registered
	 */
	public Collection<orcha.lang.programlifecycle.StepExecution> getStepExecutions() {
		Collection<orcha.lang.programlifecycle.StepExecution> stepExecutions = new ArrayList<orcha.lang.programlifecycle.StepExecution>()
		Collection<org.springframework.batch.core.StepExecution> springBatchStepExecutions = jobExecution.getStepExecutions()
		springBatchStepExecutions.each{
			stepExecutions.add(new orcha.lang.programlifecycle.StepExecution(springBatchStepExecution: it))
		}
		return stepExecutions
	}
	
	public boolean isRunning() {
		return programLifeCycleImplantation.isRunning()
	}
	
	def stopExecution(){
		jobExecution.stop()
	}

}
