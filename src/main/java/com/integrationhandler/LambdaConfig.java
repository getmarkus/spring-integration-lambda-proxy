package com.integrationhandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.messaging.Message;

import com.integrationhandler.domain.GreetingMessage;

@Configuration
@EnableIntegration
@IntegrationComponentScan
public class LambdaConfig {

    /**
     * Main gateway into routers
     */
    @MessagingGateway
    public interface ControllerGateway {
        @Gateway(requestChannel="requestChannel")
        public Message<?> route(Message<String> message);
    }

    /**
     * Initial flow that routes either directly to a controller or 
     * to another channel for type conversion json --> object
     */
    @Bean
    public IntegrationFlow requestFlow(){
        return IntegrationFlows.from("requestChannel")
                                .route(new HeaderValueRouter("route"))
                                .get();
    }

    /**
     * Secondary flow that transforms json --> object and routes to controller
     */
    @Bean
    public IntegrationFlow greeIntegrationFlow(){
        return IntegrationFlows.from("POST-greeting")
                                .transform(Transformers.fromJson(GreetingMessage.class))
                                .channel("POST-greeting-activator")
                                .get();
    }

}