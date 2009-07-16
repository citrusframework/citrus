package com.consol.citrus.samples.flightbooking.model;

import java.util.ArrayList;
import java.util.List;

public class TravelBookingRequestMessage {
    String correlationId;
    Customer customer;
    List<Booking> bookings = new ArrayList<Booking>();
    
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
     * @return the customer
     */
    public Customer getCustomer() {
        return customer;
    }
    /**
     * @param customer the customer to set
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
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
