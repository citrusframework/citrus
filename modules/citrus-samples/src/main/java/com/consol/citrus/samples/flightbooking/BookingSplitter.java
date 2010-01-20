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
import org.springframework.integration.splitter.AbstractMessageSplitter;

import com.consol.citrus.samples.flightbooking.model.*;

public class BookingSplitter extends AbstractMessageSplitter {
    
    @Override
    public Object splitMessage(Message<?> message) {
        List<Message<FlightBookingRequestMessage>> flightRequests = new ArrayList<Message<FlightBookingRequestMessage>>();
        
        if(message.getPayload() instanceof TravelBookingRequestMessage == false) {
            throw new IllegalStateException("Unsupported message type: " + message.getPayload().getClass());
        }
        
        TravelBookingRequestMessage request  = ((TravelBookingRequestMessage)message.getPayload());
        
        for (Flight flight : request.getFlights()) {
            FlightBookingRequestMessage flightRequest = new FlightBookingRequestMessage();
            flightRequest.setFlight(flight);
            flightRequest.setCorrelationId(request.getCorrelationId());
            flightRequest.setCustomer(request.getCustomer());
            flightRequest.setBookingId("myBookingId");
            
            MessageBuilder<FlightBookingRequestMessage> messageBuilder = MessageBuilder.withPayload(flightRequest);
            messageBuilder.copyHeaders(message.getHeaders());
            
            flightRequests.add(messageBuilder.build());
        }
        
        return flightRequests;
    }
}
