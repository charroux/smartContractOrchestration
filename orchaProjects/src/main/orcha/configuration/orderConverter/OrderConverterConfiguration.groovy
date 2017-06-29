
package configuration.orderConverter;

import groovy.util.logging.Slf4j;
import orcha.lang.configuration.Application;
import orcha.lang.configuration.EventHandler;
import orcha.lang.configuration.Input;
import orcha.lang.configuration.InputFileAdapter;
import orcha.lang.configuration.JavaServiceAdapter;
import orcha.lang.configuration.Output;
import orcha.lang.configuration.OutputFileAdapter;
import orcha.lang.configuration.ScriptServiceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import service.order.VendorOrderConverter;


/**
 * Auto generated configuration file due to missing configuration detail.
 * Edit this file to improve the configuration.
 * This file won't be generated again once it has been edited.
 * Delete it to generate a new one (any added configuration will be discarded)
 * 
 */
@Configuration
@Slf4j
public class OrderConverterConfiguration {


    @Bean
    public Application orderConverter1() {
        Application application = new Application();
        application.setName("orderConverter1");
        application.setLanguage("Groovy");
        JavaServiceAdapter javaAdapter = new JavaServiceAdapter();
        javaAdapter.setJavaClass("service.order.VendorOrderConverter");
        javaAdapter.setMethod("convert");
        Input input = new Input();
        input.setType("service.order.Order");
        input.setAdapter(javaAdapter);
        application.setInput(input);
        Output output = new Output();
        output.setType("service.order.SpecificOrder");
        output.setAdapter(javaAdapter);
        application.setOutput(output);
        return application;
    }

    @Bean
    public Application orderConverter2() {
        Application application = new Application();
        application.setName("orderConverter2");
        application.setLanguage("js");
        ScriptServiceAdapter scriptAdapter = new ScriptServiceAdapter();
        scriptAdapter.setFile("file:src/main/orcha/service/order/orderConverter2.js");
        Input input = new Input();
        input.setType("service.order.Order");
        input.setAdapter(scriptAdapter);
        application.setInput(input);
        Output output = new Output();
        output.setType("service.order.SpecificOrder");
        output.setAdapter(scriptAdapter);
        application.setOutput(output);
        return application;
    }

    @Bean
    public VendorOrderConverter vendorOrderConverter() {
        VendorOrderConverter service = new VendorOrderConverter();
        return service;
    }

    @Bean
    public Application selectOrderConverter() {
        Application application = new Application();
        application.setName("selectOrderConverter");
        application.setDescription("selection of a service offer");
        Input input = new Input();
        input.setType("java.util.List<service.order.SpecificOrder>");
        application.setInput(input);
        Output output = new Output();
        output.setType("service.order.SpecificOrder");
        application.setOutput(output);
        return application;
    }

    @Bean
    public EventHandler orderEventHandler() {
        EventHandler eventHandler = new EventHandler();
        eventHandler.setName("orderEventHandler");
        InputFileAdapter localFileAdapter = new InputFileAdapter();
        localFileAdapter.setDirectory("C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/OrchaBeforeLibrary/orchaProjects/src/main/resources/data/orderConverter/orderEventHandler");
        Input input = new Input();
        input.setType("service.order.Order");
        input.setMimeType("application/json");
        input.setAdapter(localFileAdapter);
        eventHandler.setInput(input);
        return eventHandler;
    }

    @Bean
    public EventHandler orderConverterOutput() {
        EventHandler eventHandler = new EventHandler();
        eventHandler.setName("orderConverterOutput");
        OutputFileAdapter localFileAdapter = new OutputFileAdapter();
        localFileAdapter.setDirectory("C:/Users/Charroux_std/Documents/projet/ExecAndShare/orcha/Orcha/OrchaBeforeLibrary/orchaProjects/src/main/resources/data/orderConverter/orderConverterOutput");
        localFileAdapter.setCreateDirectory(true);
        localFileAdapter.setAppendNewLine(true);
        localFileAdapter.setWritingMode((orcha.lang.configuration.OutputFileAdapter.WritingMode.APPEND));
        Output output = new Output();
        output.setType("service.order.SpecificOrder");
        output.setAdapter(localFileAdapter);
        eventHandler.setOutput(output);
        return eventHandler;
    }

}
