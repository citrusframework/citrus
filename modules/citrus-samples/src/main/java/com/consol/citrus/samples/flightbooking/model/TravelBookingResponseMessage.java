package com.consol.citrus.samples.flightbooking.model;

import java.util.ArrayList;
import java.util.List;

public class TravelBookingResponseMessage {
    String correlationId;
    boolean success;
    List<Booking> bookings = new ArrayList<Booking>();
    
    String xmlns = "http://www.consol.com/schemas/FlightBooking/TravelAgency/TravelAgencySchema.xsd";
    
    /**
     * @return the correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }
    /**
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }
    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    /**
     * @return the bookings
     */
    public List<Booking> getBookings() {
        return bookings;
    }
    /**
     * @param bookings the bookings to set
     */
    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}
