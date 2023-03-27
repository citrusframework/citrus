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

package org.citrusframework.citrus.demo;

import java.io.IOException;

import org.citrusframework.citrus.demo.model.HelloRequest;
import org.citrusframework.citrus.demo.model.HelloResponse;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.xml.StringResult;
import org.citrusframework.citrus.xml.StringSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;

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
            throw new CitrusRuntimeException("Failed to marshal/unmarshal XML", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed due to IO error", e);
        }
    }

    public abstract Message<HelloResponse> sayHello(Message<HelloRequest> requestMessage);
}
