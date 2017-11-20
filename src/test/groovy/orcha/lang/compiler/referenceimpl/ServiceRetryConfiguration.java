package orcha.lang.compiler.referenceimpl;

import orcha.lang.configuration.Application;
import orcha.lang.configuration.Retry;

@Retry(maxNumberOfAttempts=3, intervalBetweenTheFirstAndSecondAttempt=1000L, intervalMultiplierBetweenAttemps=2, maximumIntervalBetweenAttempts=4000L)
class ServiceRetryConfiguration extends Application{
}