package com.consol.citrus.samples.flightbooking;

import java.util.List;

import com.consol.citrus.samples.flightbooking.model.FlightBookingConfirmationMessage;

public class FlightCompletionStrategy {
    
    public boolean isComplete(List<FlightBookingConfirmationMessage> messages) {
        return messages.size() % 2 == 0;
    }

}
