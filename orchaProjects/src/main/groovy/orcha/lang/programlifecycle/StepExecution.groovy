package orcha.lang.programlifecycle

import java.util.Date;
import java.util.List;

class StepExecution {
	
	org.springframework.batch.core.StepExecution springBatchStepExecution
	
	/**
	 * Returns the time that this execution ended
	 *
	 * @return the time that this execution ended
	 */
	public Date getEndTime() {
		return springBatchStepExecution.endTime;
	}

	/**
	 * Gets the time this execution started
	 *
	 * @return the time this execution started
	 */
	public Date getStartTime() {
		return springBatchStepExecution.startTime;
	}
	
	/**
	 * Returns the current status of this step
	 *
	 * @return the current status of this step
	 */
	public ProgramStatus getStatus() {
		return ProgramStatus.valueOf(springBatchStepExecution.status.name());
	}
	
	/**
	 * @return the name of the step
	 */
	public String getStepName() {
		return springBatchStepExecution.stepName;
	}
	
	public List<Throwable> getFailureExceptions() {
		return springBatchStepExecution.failureExceptions;
	}

}
