package com.consol.citrus.samples.flightbooking.model;


public class Booking {
    String flightId;
    String airline;
    String from;
    String to;
    String date;
    String scheduledDeparture;
    String scheduledArrival;
    
    /**
     * @return the flightId
     */
    public String getFlightId() {
        return flightId;
    }
    /**
     * @param flightId the flightId to set
     */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }
    /**
     * @return the airline
     */
    public String getAirline() {
        return airline;
    }
    /**
     * @param airline the airline to set
     */
    public void setAirline(String airline) {
        this.airline = airline;
    }
    /**
     * @return the from
     */
    public String getFrom() {
        return from;
    }
    /**
     * @param from the from to set
     */
    public void setFrom(String from) {
        this.from = from;
    }
    /**
     * @return the to
     */
    public String getTo() {
        return to;
    }
    /**
     * @param to the to to set
     */
    public void setTo(String to) {
        this.to = to;
    }
    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }
    /**
     * @return the scheduledDeparture
     */
    public String getScheduledDeparture() {
        return scheduledDeparture;
    }
    /**
     * @param scheduledDeparture the scheduledDeparture to set
     */
    public void setScheduledDeparture(String scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }
    /**
     * @return the scheduledArrival
     */
    public String getScheduledArrival() {
        return scheduledArrival;
    }
    /**
     * @param scheduledArrival the scheduledArrival to set
     */
    public void setScheduledArrival(String scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
    }
}
