package com.integrationhandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationhandler.LambdaConfig.ControllerGateway;
import com.amazonaws.services.lambda.runtime.Context;

@SpringBootApplication
public class IntegrationProxyApplication implements RequestHandler<ServerlessInput,ServerlessOutput> {

	private boolean initialized;
	private ApplicationContext applicationContext;
	private ControllerGateway gateway;
	/**
	 * Traditional Spring Boot entry
	 */
	public static void main(String[] args) {
		SpringApplication.run(LambdaConfig.class, args);
	}

	/**
	 * Lambda proxy entry point
	 */
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		if(!initialized) {
			applicationContext = new SpringApplicationBuilder(LambdaConfig.class)
				.web(false) //tells Spring not to setup a servlet and listen on port
				.run(new String[]{});

			initialized = true;
		}

		Message<String> message = MessageBuilder.withPayload(serverlessInput.getBody())
			.setHeader("route", (serverlessInput.getHttpMethod()
									+ serverlessInput.getPath()).replace('/', '-'))
			.build();

		gateway = applicationContext.getBean(ControllerGateway.class);

		Message<?> response = gateway.route(message);

		ObjectMapper mapper = new ObjectMapper();

		ServerlessOutput output = new ServerlessOutput();

		try {
			output.setBody(mapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(response.getPayload()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output;
	}
}
