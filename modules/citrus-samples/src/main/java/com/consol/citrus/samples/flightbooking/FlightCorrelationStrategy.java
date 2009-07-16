package com.consol.citrus.samples.flightbooking;

import com.consol.citrus.samples.flightbooking.model.FlightBookingConfirmationMessage;

public class FlightCorrelationStrategy {
    
    public Object getCorrelationKey(FlightBookingConfirmationMessage message) {
        return message.getCorrelationId();
    }
}
