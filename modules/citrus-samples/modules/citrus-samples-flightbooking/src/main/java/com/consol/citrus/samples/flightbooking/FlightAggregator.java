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

package com.consol.citrus.samples.flightbooking;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.samples.flightbooking.model.*;

/**
 * @author Christoph Deppisch
 */
public class FlightAggregator {

    public Message<TravelBookingResponseMessage> processFlights(List<FlightBookingConfirmationMessage> messages) {
        TravelBookingResponseMessage responseMessage = new TravelBookingResponseMessage();
        
        List<Flight> flights = new ArrayList<Flight>();
        for (FlightBookingConfirmationMessage confirmationMessage : messages) {
            flights.add(confirmationMessage.getFlight());
        }
        
        responseMessage.setFlights(flights);
        responseMessage.setCorrelationId(messages.get(0).getCorrelationId());
        responseMessage.setSuccess(true);
        
        MessageBuilder<TravelBookingResponseMessage> messageBuilder = MessageBuilder.withPayload(responseMessage);
        messageBuilder.setHeader("correlationId", responseMessage.getCorrelationId());
        
        return messageBuilder.build();
    }
}
