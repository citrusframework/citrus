package com.consol.citrus.demo;

import org.springframework.integration.core.Message;

public interface HelloService {
    public Message<HelloResponseMessage> sayHello(Message<HelloRequestMessage> request);
}
