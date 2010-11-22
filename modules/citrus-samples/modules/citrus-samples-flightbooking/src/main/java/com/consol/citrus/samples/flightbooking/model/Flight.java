/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.flightbooking.model;

import javax.persistence.*;

/**
 * @author Christoph Deppisch
 */
@Entity
@Table(name = "FBS_FLIGHT")
public class Flight {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "FLIGHT_ID")
    private String flightId;
    
    @Column(name = "AIRLINE", length = 150, nullable = false)
    private String airline;
    
    @Column(name = "FROM_AIRPORT", length = 3, nullable = false)
    private String fromAirport;
    
    @Column(name = "TO_AIRPORT", length = 3, nullable = false)
    private String toAirport;
    
    @Column(name = "DATE", length = 20, nullable = false)
    private String date;
    
    @Column(name = "SCHEDULED_DEPARTURE", length = 40, nullable = false)
    private String scheduledDeparture;
    
    @Column(name = "SCHEDULED_ARRIVAL", length = 40, nullable = false)
    private String scheduledArrival;
    
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
    public String getFromAirport() {
        return fromAirport;
    }
    /**
     * @param from the from to set
     */
    public void setFromAirport(String from) {
        this.fromAirport = from;
    }
    /**
     * @return the to
     */
    public String getToAirport() {
        return toAirport;
    }
    /**
     * @param to the to to set
     */
    public void setToAirport(String to) {
        this.toAirport = to;
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
