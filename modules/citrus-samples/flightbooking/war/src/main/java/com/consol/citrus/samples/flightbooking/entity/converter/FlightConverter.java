/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.samples.flightbooking.entity.converter;

import com.consol.citrus.samples.flightbooking.entity.FlightEntity;
import com.consol.citrus.samples.flightbooking.model.Flight;
import org.springframework.util.StringUtils;

import java.text.*;
import java.util.Calendar;

/**
 * Converter takes care on model to entity conversion and vice versa.
 * @author Christoph Deppisch
 */
public class FlightConverter {

    /**
     * Prevent instantiation.
     */
    private FlightConverter() {
    }
    
    /**
     * Get model form entity.
     * @param entity
     * @return
     * @throws ParseException 
     */
    public static Flight from(FlightEntity entity) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss");

        if (entity ==  null) {
            return null;
        }
        
        Flight model = new Flight();

        Calendar scheduledArrival = null;
        Calendar scheduledDeparture = null;
        try {
            if (StringUtils.hasText(entity.getScheduledArrival())) {
                scheduledArrival = Calendar.getInstance();
                scheduledArrival.setTime(dateFormat.parse(entity.getScheduledArrival()));
            }
            
            if (StringUtils.hasText(entity.getScheduledDeparture())) {
                scheduledDeparture = Calendar.getInstance();
                scheduledDeparture.setTime(dateFormat.parse(entity.getScheduledDeparture()));
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse date format", e);
        }
        
        model.setAirline(entity.getAirline());
        model.setFlightId(entity.getFlightId());
        model.setFromAirport(entity.getFromAirport());
        model.setScheduledArrival(scheduledArrival);
        model.setToAirport(entity.getToAirport());
        model.setScheduledDeparture(scheduledDeparture);
        
        return model;
    }
    
    /**
     * Get entity form model.
     * @param model
     * @return
     */
    public static FlightEntity from(Flight model) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss");

        FlightEntity entity = new FlightEntity();
        
        entity.setAirline(model.getAirline());
        entity.setFlightId(model.getFlightId());
        entity.setFromAirport(model.getFromAirport());
        entity.setScheduledArrival(dateFormat.format(model.getScheduledArrival().getTime()));
        entity.setToAirport(model.getToAirport());
        entity.setScheduledDeparture(dateFormat.format(model.getScheduledDeparture().getTime()));
        
        return entity;
    }
}
