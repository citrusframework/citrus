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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 */
public class TravelBookingResponseMessage {
    private String correlationId;
    
    private boolean success;
    
    private List<Flight> flights = new ArrayList<Flight>();
    
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
     * @return the flights
     */
    public List<Flight> getFlights() {
        return flights;
    }
    
    /**
     * @param flights the flights to set
     */
    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }
    
}
