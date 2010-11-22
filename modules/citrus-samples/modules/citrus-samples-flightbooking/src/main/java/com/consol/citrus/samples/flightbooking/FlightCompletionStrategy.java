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

package com.consol.citrus.samples.flightbooking;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.samples.flightbooking.model.FlightBookingConfirmationMessage;

/**
 * @author Christoph Deppisch
 */
public class FlightCompletionStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(FlightCompletionStrategy.class);
    
    private Map<String, Integer> completionRules = new HashMap<String, Integer>();
    
    public boolean isComplete(List<FlightBookingConfirmationMessage> messages) {
        if(messages.size() == 0) {return false;}
        
        boolean isComplete = messages.size() == completionRules.get(messages.get(0).getCorrelationId());
        
        log.info("FlightAggregator (" + messages.get(0).getCorrelationId()
                + ") complete = " + isComplete);
        
        return isComplete;
    }
    
    public void addCompletionRule(String correlationLKey, Integer completionRuleSize) {
        completionRules.put(correlationLKey, completionRuleSize);
    }
}
