package com.consol.citrus.samples.flightbooking;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.samples.flightbooking.model.Booking;
import com.consol.citrus.samples.flightbooking.model.FlightBookingConfirmationMessage;
import com.consol.citrus.samples.flightbooking.model.TravelBookingResponseMessage;

public class FlightAggregator {

    public Message<TravelBookingResponseMessage> processFlights(List<FlightBookingConfirmationMessage> messages) {
        TravelBookingResponseMessage responseMessage = new TravelBookingResponseMessage();
        
        List<Booking> bookings = new ArrayList<Booking>();
        for (FlightBookingConfirmationMessage confirmationMessage : messages) {
            Booking booking = new Booking();
            booking.setAirline(confirmationMessage.getFlight().getAirline());
            booking.setFlightId(confirmationMessage.getFlight().getFlightId());
            booking.setFrom(confirmationMessage.getFlight().getFrom());
            booking.setTo(confirmationMessage.getFlight().getTo());
            booking.setDate(confirmationMessage.getFlight().getDate());
            booking.setScheduledArrival(confirmationMessage.getFlight().getScheduledArrival());
            booking.setScheduledDeparture(confirmationMessage.getFlight().getScheduledDeparture());
            
            bookings.add(booking);
        }
        
        responseMessage.setBookings(bookings);
        responseMessage.setCorrelationId(messages.get(0).getCorrelationId());
        responseMessage.setSuccess(true);
        
        MessageBuilder<TravelBookingResponseMessage> messageBuilder = MessageBuilder.withPayload(responseMessage);
        messageBuilder.setHeader("correlationId", responseMessage.getCorrelationId());
        
        return messageBuilder.build();
    }
}
