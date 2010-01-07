/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.samples.greeting;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.samples.greeting.model.GreetingRequestMessage;
import com.consol.citrus.samples.greeting.model.GreetingResponseMessage;

public class GreetingService {

    public Message<GreetingResponseMessage> sayHello(Message<GreetingRequestMessage> request) {
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
