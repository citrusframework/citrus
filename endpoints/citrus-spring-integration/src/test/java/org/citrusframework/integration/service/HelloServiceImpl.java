/*
 * Copyright the original author or authors.
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

package org.citrusframework.integration.service;

import org.citrusframework.integration.service.model.HelloRequest;
import org.citrusframework.integration.service.model.HelloResponse;
import org.springframework.messaging.Message;
import org.springframework.integration.support.MessageBuilder;

public class HelloServiceImpl extends AbstractMarshallingHelloService {

    public Message<HelloResponse> sayHello(Message<HelloRequest> request) {
        HelloResponse response = new HelloResponse();
        response.setMessageId(request.getPayload().getMessageId());
        response.setCorrelationId(request.getPayload().getCorrelationId());
        response.setUser("HelloService");
        response.setText("Hello " + request.getPayload().getUser());

        MessageBuilder<HelloResponse> builder = MessageBuilder.withPayload(response);
        builder.setHeader("CorrelationId", request.getHeaders().get("CorrelationId"));
        builder.setHeader("Operation", "sayHello");
        builder.setHeader("Type", "response");

        return builder.build();
    }
}
