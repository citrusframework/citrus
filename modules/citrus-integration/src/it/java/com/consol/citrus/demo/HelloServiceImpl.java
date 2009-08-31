package com.consol.citrus.demo;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

public class HelloServiceImpl implements HelloService {

    public Message<HelloResponseMessage> sayHello(Message<HelloRequestMessage> request) {
        HelloResponseMessage helloResponse = new HelloResponseMessage();
        helloResponse.setMessageId(request.getPayload().getMessageId());
        helloResponse.setCorrelationId(request.getPayload().getCorrelationId());
        helloResponse.setUser("HelloService");
        helloResponse.setText("Hello " + request.getPayload().getUser());
        
        MessageBuilder<HelloResponseMessage> builder = MessageBuilder.withPayload(helloResponse);
        builder.setHeader("CorrelationId", request.getHeaders().get("CorrelationId"));
        builder.setHeader("Operation", "sayHello");
        builder.setHeader("Type", "response");
        
        return builder.build();
    }

}
