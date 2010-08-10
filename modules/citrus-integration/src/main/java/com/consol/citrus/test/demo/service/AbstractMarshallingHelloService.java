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

package com.consol.citrus.test.demo.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.oxm.*;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import com.consol.citrus.test.demo.model.HelloRequest;
import com.consol.citrus.test.demo.model.HelloResponse;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractMarshallingHelloService implements HelloService {

    @Autowired
    private Marshaller helloMarshaller;
    
    @Autowired
    private Unmarshaller helloUnmarshaller;
    
    @ServiceActivator
    public Message<String> sayHelloInternal(Message<String> request) {
        try {
            Message<HelloRequest> helloRequest = MessageBuilder
                    .withPayload((HelloRequest) helloUnmarshaller.unmarshal(new StringSource(request.getPayload())))
                    .copyHeaders(request.getHeaders())
                    .build();

            StringResult result = new StringResult();
            helloMarshaller.marshal(sayHello(helloRequest).getPayload(), result);
            
            return MessageBuilder.withPayload(result.toString()).copyHeaders(request.getHeaders()).build();
            
        } catch (XmlMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return MessageBuilder.withPayload("").build();
    }
    
    public abstract Message<HelloResponse> sayHello(Message<HelloRequest> requestMessage);
}
