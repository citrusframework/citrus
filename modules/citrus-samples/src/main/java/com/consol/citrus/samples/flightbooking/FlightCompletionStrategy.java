package com.consol.citrus.samples.flightbooking;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.samples.flightbooking.model.FlightBookingConfirmationMessage;

public class FlightCompletionStrategy {
    
    Logger log = LoggerFactory.getLogger(FlightCompletionStrategy.class);
    
    public boolean isComplete(List<FlightBookingConfirmationMessage> messages) {
        log.info("FlightAggregator (" + messages.get(0).getCorrelationId()
                + ") complete = " + (messages.size() == 2));
        
        return messages.size() == 2;
    }

}
