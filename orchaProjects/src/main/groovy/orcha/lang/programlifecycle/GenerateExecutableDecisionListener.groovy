package orcha.lang.programlifecycle

import groovy.util.logging.Slf4j;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;

@Slf4j
class GenerateExecutableDecisionListener extends StepExecutionListenerSupport {
	
    public ExitStatus afterStep(StepExecution stepExecution) {
			
        if (stepExecution.getExitStatus() == ExitStatus.FAILED) {
			log.info "no executable generation"
            return new ExitStatus("NO EXECUTABLE GENERATION");
        }
        else {
			log.info "generate executable"
            return new ExitStatus("GENERATE EXECUTABLE");
        }
    }
}