
package configuration.order;

import groovy.util.logging.Slf4j;
import orcha.lang.configuration.Application;
import orcha.lang.configuration.EventHandler;
import orcha.lang.configuration.InputFileAdapter;
import orcha.lang.configuration.JavaServiceAdapter;
import orcha.lang.configuration.OutputFileAdapter;
import static org.mockito.Mockito.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import service.order.Order;
import service.order.SpecificOrder;


/**
 * Auto generated configuration file due to missing configuration detail.
 * Edit this file to improve the configuration.
 * This file won't be generated again once it has been edited.
 * Delete it to generate a new one (any added configuration will be discarded)
 * 
 */
@Configuration
@Slf4j
public class SelectbestTVvendorsConfiguration
    extends BenchmarkingVendorsConfiguration
{


    @Bean
    public OrderConverterService orderConverterService() {
        OrderConverterService orderConverterServiceMock = mock((configuration.order.OrderConverterService.class));
        Order mockInput = new Order();
        SpecificOrder mockOutput = new SpecificOrder();
        when(orderConverterServiceMock.service(mockInput)).thenReturn(mockOutput);
        return orderConverterServiceMock;
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

    @Bean
    @Override
    public EventHandler customer() {
        InputFileAdapter localFileAdapter = new InputFileAdapter();
        localFileAdapter.setDirectory("C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/OrchaBeforeLibrary/bin/data/order/customer/");
        EventHandler eventHandler = super.customer();
        eventHandler.getInput().setAdapter(localFileAdapter);
        return eventHandler;
    }

    @Bean
    @Override
    public EventHandler outputFile1() {
        OutputFileAdapter localFileAdapter = new OutputFileAdapter();
        localFileAdapter.setDirectory("C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/OrchaBeforeLibrary/bin/data/order/outputFile1");
        localFileAdapter.setCreateDirectory(true);
        localFileAdapter.setAppendNewLine(true);
        localFileAdapter.setWritingMode((orcha.lang.configuration.OutputFileAdapter.WritingMode.APPEND));
        EventHandler eventHandler = super.outputFile1();
        eventHandler.getOutput().setAdapter(localFileAdapter);
        return eventHandler;
    }

}
