/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
