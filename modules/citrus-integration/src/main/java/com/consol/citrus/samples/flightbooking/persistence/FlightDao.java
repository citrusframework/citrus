/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.samples.flightbooking.persistence;

import java.util.List;

import com.consol.citrus.samples.flightbooking.model.Flight;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
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
