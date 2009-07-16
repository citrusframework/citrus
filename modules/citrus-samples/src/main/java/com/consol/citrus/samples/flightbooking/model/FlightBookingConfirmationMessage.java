package com.consol.citrus.samples.flightbooking.model;

public class FlightBookingConfirmationMessage {
    String correlationId;
    String bookingId;
    boolean success;
    Flight flight;
    
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
     * @return the bookingId
     */
    public String getBookingId() {
        return bookingId;
    }
    /**
     * @param bookingId the bookingId to set
     */
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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
     * @return the flight
     */
    public Flight getFlight() {
        return flight;
    }
    /**
     * @param flight the flight to set
     */
    public void setFlight(Flight flight) {
        this.flight = flight;
    }
}
