/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.jms.integration.service;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.integration.service.model.HelloRequest;
import com.consol.citrus.jms.integration.service.model.HelloResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.oxm.*;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessageFactory;

import java.io.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class HelloSoapServiceImpl {

    @Autowired
    private SoapMessageFactory messageFactory;

    @Autowired
    private Marshaller marshaller;

    @Autowired
    private Unmarshaller unmarshaller;

    @ServiceActivator
    public Message<String> sayHello(Message<String> request) {
        WebServiceMessage webServiceRequest;
        try {
            webServiceRequest = messageFactory.createWebServiceMessage(new ByteArrayInputStream(request.getPayload().getBytes()));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read SOAP request", e);
        }

        try {
            HelloRequest helloRequest = (HelloRequest) unmarshaller.unmarshal(webServiceRequest.getPayloadSource());

            HelloResponse response = new HelloResponse();
            response.setMessageId(helloRequest.getMessageId());
            response.setCorrelationId(helloRequest.getCorrelationId());
            response.setUser("HelloSoapService");
            response.setText("Hello " + helloRequest.getUser());

            WebServiceMessage webServiceResponse = messageFactory.createWebServiceMessage();
            marshaller.marshal(response, webServiceResponse.getPayloadResult());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            webServiceResponse.writeTo(bos);

            return MessageBuilder.withPayload(new String(bos.toByteArray())).build();
        } catch (XmlMappingException e) {
            throw new CitrusRuntimeException("Failed to marshal/unmarshal XML", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed due to IO error", e);
        }
    }
}
