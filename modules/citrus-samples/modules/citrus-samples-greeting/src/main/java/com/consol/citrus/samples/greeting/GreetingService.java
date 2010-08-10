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

package com.consol.citrus.samples.greeting;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.samples.common.AbstractMarshallingMessageService;
import com.consol.citrus.samples.greeting.model.GreetingRequestMessage;
import com.consol.citrus.samples.greeting.model.GreetingResponseMessage;

/**
 * @author Christoph Deppisch
 */
public class GreetingService extends AbstractMarshallingMessageService<GreetingRequestMessage, GreetingResponseMessage> {

    @Override
    public Message<GreetingResponseMessage> processMessage(Message<GreetingRequestMessage> request) {
        GreetingResponseMessage response = new GreetingResponseMessage();
        response.setOperation(request.getPayload().getOperation());
        response.setCorrelationId(request.getPayload().getCorrelationId());
        response.setUser("GreetingService");
        response.setText("Hello " + request.getPayload().getUser() + "!");
        
        MessageBuilder<GreetingResponseMessage> builder = MessageBuilder.withPayload(response);
        builder.setHeader("CorrelationId", request.getHeaders().get("CorrelationId"));
        builder.setHeader("Operation", "sayHello");
        builder.setHeader("Type", "response");
        
        return builder.build();
    }

}
