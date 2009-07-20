package com.consol.citrus.samples.flightbooking;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.samples.flightbooking.model.*;

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
