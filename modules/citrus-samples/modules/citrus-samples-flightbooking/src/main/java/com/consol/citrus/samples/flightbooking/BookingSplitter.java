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

import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.samples.flightbooking.model.*;
import com.consol.citrus.samples.flightbooking.persistence.CustomerDao;
import com.consol.citrus.samples.flightbooking.persistence.FlightDao;

/**
 * @author Christoph Deppisch
 */
public class BookingSplitter {
    private CustomerDao customerDao;
    
    private FlightDao flightDao;
    
    private FlightCompletionStrategy flightCompletionStrategy;
    
    @Splitter
    public Object splitMessage(Message<?> message) {
        List<Message<FlightBookingRequestMessage>> flightRequests = new ArrayList<Message<FlightBookingRequestMessage>>();
        
        if(!(message.getPayload() instanceof TravelBookingRequestMessage)) {
            throw new IllegalStateException("Unsupported message type: " + message.getPayload().getClass());
        }
        
        TravelBookingRequestMessage request  = ((TravelBookingRequestMessage)message.getPayload());
        
        //Save customer if not already present
        if(customerDao.find(request.getCustomer().getId()) == null) {
            customerDao.persist(request.getCustomer());
        }
        
        for (Flight flight : request.getFlights()) {
            //Save flight if necessary
            if (flightDao.find(flight.getFlightId()) == null) {
                flightDao.persist(flight);
            }
            
            FlightBookingRequestMessage flightRequest = new FlightBookingRequestMessage();
            flightRequest.setFlight(flight);
            flightRequest.setCorrelationId(request.getCorrelationId());
            flightRequest.setCustomer(request.getCustomer());
            flightRequest.setBookingId("myBookingId");
            
            MessageBuilder<FlightBookingRequestMessage> messageBuilder = MessageBuilder.withPayload(flightRequest);
            messageBuilder.copyHeaders(message.getHeaders());
            
            flightRequests.add(messageBuilder.build());
        }
        
        flightCompletionStrategy.addCompletionRule(request.getCorrelationId(), request.getFlights().size());
        
        return flightRequests;
    }

    /**
     * @param customerDao the customerDao to set
     */
    public void setCustomerDao(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /**
     * @param flightDao the flightDao to set
     */
    public void setFlightDao(FlightDao flightDao) {
        this.flightDao = flightDao;
    }

    /**
     * @param flightCompletionStrategy the flightCompletionStrategy to set
     */
    public void setFlightCompletionStrategy(
            FlightCompletionStrategy flightCompletionStrategy) {
        this.flightCompletionStrategy = flightCompletionStrategy;
    }
}
