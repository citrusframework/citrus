package com.consol.citrus.samples.flightbooking.model;

public class FlightBookingRequestMessage {
    String correlationId;
    String bookingId;
    Customer customer;
    Flight flight;
    
    String xmlns = "http://www.consol.com/schemas/FlightBooking/AirlineSchema.xsd";
    
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
