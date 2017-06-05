# spring-integration-lambda-proxy

There are several projects connecting the last mile between the AWS API Gateway and AWS Lambda.  In the summer of 2016, the only available integration was a one-to-one mapping of an API Gateway Resource and a Lambda Function.  There were ways to make it a somewhat efficient developer work flow.  However, it produced annoying friction.

Last November 2016, AWS introduced the 'Proxy' interface.  This interface allows the API Gateway to proxy several resource endpoints to a single Lambda function. However, this mostly just pushed the issue down the road. This is because you need to now have an efficient way to distribute the requests to your code within that single Lambda Function.

One such interesting project aiming to reduce that friction is the [AWS Serverless Container](https://github.com/awslabs/aws-serverless-java-container). It attempts to seamlessly connect your Lambda Proxy entry point to your code. It has interfaces for Spring, Jersey and Spark. It and other projects like it pass the Lambda Proxy request to a servlet/dispatcher front controller.

However, when I tried to do anything real or bring in other Spring projects, the constraints often got in the way and soon became counter productive. Also, it seemed like these projects were trying to enforce the wrong concern.  AWS Lambda feels much more event-driven and has a distinct message passing scheme. Rather than a request-response MVC front controller.

So in this project, I tired to solve the issue by implementing message routing via [Spring Integration](https://projects.spring.io/spring-integration/).  Additionally, you can run your code locally via Spring Boot.

A key to make Spring Boot play nice with Lambda is to tell it not run a full web environment:

```java
public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {

		if(!initialized) {

			applicationContext = new SpringApplicationBuilder(LambdaConfig.class)

				.web(false) //tells Spring not to setup a servlet and listen on port

				.run(new String[]{});

			initialized = true;

		}
```

After wrapping the Lambda request body in a Spring Integration Message envelope, then one option to route is to set a header. You can use whatever scheme makes sense for you.

```java
Message<String> message = MessageBuilder.withPayload(serverlessInput.getBody())

			.setHeader("route", (serverlessInput.getHttpMethod()

									+ serverlessInput.getPath()).replace('/', '-'))

			.build();

```

Four key parts of Spring Integration are needed:

1. Messaging Gateway
```java
@MessagingGateway

    public interface ControllerGateway {

        @Gateway(requestChannel="requestChannel")

        public Message<?> route(Message<String> message);

    }
```
2. Integration Flow to, typically, route to a deserializer
```java
@Bean

    public IntegrationFlow requestFlow(){

        return IntegrationFlows.from("requestChannel")

                                .route(new HeaderValueRouter("route"))

                                .get();

    }
```
3. Integration Flow to route to a Controller or Resource
```java
@Bean

    public IntegrationFlow greeIntegrationFlow(){

        return IntegrationFlows.from("POST-greeting")

                                .transform(Transformers.fromJson(GreetingMessage.class))

                                .channel("POST-greeting-activator")

                                .get();

    }
```
4. Service Activator triggering your logic
```java
public class GreetingController {

    @RequestMapping(method = RequestMethod.POST, produces="application/json")

    @ServiceActivator(inputChannel="POST-greeting-activator")

    public GreetingMessage reply(@RequestBody GreetingMessage greeting) {

        

        greeting.setMessage("Well...hello there");

        return greeting;

    }
}
```

This is a simple happy path and you are likely to need more complicated (and potentially automatic) routing schemes.  But Spring Integration provides the building blocks.

Areas for improvement:
- Cold startup time with Spring (way too slow, but probably tunable)
- Need a seamless way to automatically recognize resource de-serialization

## Installation

```console
mvn clean package
```

## Usage

To run locally:
```console
mvn spring-boot:run

curl -d "{ \"message\":\"hi\" }" -H "Content-Type:application/json" -X POST http://localhost:8080/greeting

```