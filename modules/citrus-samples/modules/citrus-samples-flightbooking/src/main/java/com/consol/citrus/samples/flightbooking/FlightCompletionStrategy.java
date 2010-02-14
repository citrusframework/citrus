/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.samples.flightbooking;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.samples.flightbooking.model.FlightBookingConfirmationMessage;

/**
 * @author Christoph Deppisch
 */
public class FlightCompletionStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(FlightCompletionStrategy.class);
    
    public boolean isComplete(List<FlightBookingConfirmationMessage> messages) {
        log.info("FlightAggregator (" + messages.get(0).getCorrelationId()
                + ") complete = " + (messages.size() == 2));
        
        return messages.size() == 2;
    }

}
