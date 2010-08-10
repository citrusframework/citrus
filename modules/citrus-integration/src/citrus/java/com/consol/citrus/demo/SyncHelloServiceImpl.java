/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.demo;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.xml.transform.StringResult;

/**
 * @author Christoph Deppisch
 */
public class SyncHelloServiceImpl implements SyncHelloService  {
    
    @Autowired
    Marshaller helloMarshaller;
    
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
