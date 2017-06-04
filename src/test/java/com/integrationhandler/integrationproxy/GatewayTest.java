package com.integrationhandler.integrationproxy;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;

import com.integrationhandler.LambdaConfig.ControllerGateway;
import com.integrationhandler.domain.GreetingMessage;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GatewayTest {

    @Autowired
    private ControllerGateway gateway;

    @Test
    public void testRoute(){
        //arrange
        String messageJson = "{ \"message\":\"hi\" }";

        Message<String> message = MessageBuilder.withPayload(messageJson)
            .setHeader("route", "POST/greeting".replace('/', '-'))
            .build();

        //act
        Message<?> response = gateway.route(message);

        //assert
        assertEquals("Well...hello there", ((GreetingMessage)response.getPayload()).getMessage());

    }
}