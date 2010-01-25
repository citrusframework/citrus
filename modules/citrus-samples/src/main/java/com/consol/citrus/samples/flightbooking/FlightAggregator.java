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
