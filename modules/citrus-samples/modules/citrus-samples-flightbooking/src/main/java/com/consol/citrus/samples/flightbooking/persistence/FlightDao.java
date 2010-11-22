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

package com.consol.citrus.samples.flightbooking.persistence;

import java.util.List;

import com.consol.citrus.samples.flightbooking.model.Flight;

/**
 * @author Christoph Deppisch
 */
public interface FlightDao {

    /**
     * Load a flight with given identifyer.
     * 
     * @param flightId
     * @return
     */
    public Flight find(String flightId);
    
    /**
     * Load all available flights.
     * 
     * @return
     */
    public List<Flight> findAll();
 
    /**
     * Persist a flight.
     * 
     * @param flight
     */
    public void persist(Flight flight);
    
    /**
     * Remove a flight.
     * 
     * @param flight
     */
    public void remove(Flight flight);
    
    /**
     * Update a flight.
     * 
     * @param flight
     */
    public void merge(Flight flight);
}
