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
