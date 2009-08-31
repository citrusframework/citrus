package com.consol.citrus.demo;

import org.springframework.integration.core.Message;

public interface SyncHelloService {
    public Message<String> sayHello(Message<HelloRequestMessage> request);
}
