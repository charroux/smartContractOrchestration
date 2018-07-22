
package configuration.order;

import groovy.util.logging.Slf4j;
import orcha.lang.configuration.Application;
import orcha.lang.configuration.JavaServiceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Auto generated configuration file due to missing configuration detail.
 * Edit this file to improve the configuration.
 * This file won't be generated again once it has been edited.
 * Delete it to generate a new one (any added configuration will be discarded)
 * 
 */

@Slf4j
public class SelectbestTVvendorsConfiguration
    extends BenchmarkingVendorsConfiguration
{


    @Bean
    public OrderConverterService orderConverterService() {
        Integer test;
        return test;
    }

    @Bean
    @Override
    public Application orderConverter() {
        JavaServiceAdapter javaAdapter = new JavaServiceAdapter();
        javaAdapter.setJavaClass("OrderConverterService");
        javaAdapter.setMethod("service");
        Application application = super.orderConverter();
        application.getInput().setAdapter(javaAdapter);
        application.getOutput().setAdapter(javaAdapter);
        return application;
    }

}
