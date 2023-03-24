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

package org.citrusframework.jms.integration.service;

import java.io.IOException;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jms.integration.service.model.HelloRequest;
import org.citrusframework.jms.integration.service.model.HelloResponse;
import org.citrusframework.xml.Marshaller;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.StringSource;
import org.citrusframework.xml.Unmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

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

        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed due to IO error", e);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to marshal/unmarshal XML", e);
        }
    }

    public abstract Message<HelloResponse> sayHello(Message<HelloRequest> requestMessage);
}
