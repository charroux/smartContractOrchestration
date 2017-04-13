package orcha.lang.programlifecycle

import groovy.util.logging.Slf4j;

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListenerSupport
import org.springframework.batch.core.StepExecution

@Slf4j
class InteractiveModeDecisionListener extends StepExecutionListenerSupport {
	
    public ExitStatus afterStep(StepExecution stepExecution) {
			
        if (stepExecution.getExitStatus() == ExitStatus.FAILED) {
			log.info "batch mode"
            return new ExitStatus("BATCH MODE");
        }
        else {
			log.info "interactive mode"
            return new ExitStatus("INTERACTIVE MODE");
        }
    }
}