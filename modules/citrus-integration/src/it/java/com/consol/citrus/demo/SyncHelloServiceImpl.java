package com.consol.citrus.demo;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.xml.transform.StringResult;

public class SyncHelloServiceImpl implements SyncHelloService  {
    
    @Autowired
    XStreamMarshaller helloMarshaller;
    
    public Message<String> sayHello(Message<HelloRequestMessage> request) {
        HelloResponseMessage helloResponse = new HelloResponseMessage();
        helloResponse.setMessageId(request.getPayload().getMessageId());
        helloResponse.setCorrelationId(request.getPayload().getCorrelationId());
        helloResponse.setUser("HelloService");
        helloResponse.setText("Hello " + request.getPayload().getUser());
        
        StringResult result = new StringResult();
        try {
            helloMarshaller.marshal(helloResponse, result);
        } catch (XmlMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        MessageBuilder<String> builder = MessageBuilder.withPayload(result.toString());
        builder.setHeader("CorrelationId", request.getHeaders().get("CorrelationId"));
        builder.setHeader("Operation", "sayHello");
        builder.setHeader("Type", "response");
        
        return builder.build();
    }
}
