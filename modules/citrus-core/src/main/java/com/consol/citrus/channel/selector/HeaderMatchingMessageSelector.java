/*
 * Copyright 2006-2012 the original author or authors.
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
package com.consol.citrus.channel.selector;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.integration.core.MessageSelector;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Message selector matches one or more header elements with the message header. Only in case all 
 * matching header elements are present in message header and its value matches the expected value
 * the message is accepted.
 * 
 * @author Christoph Deppisch
 */
public class HeaderMatchingMessageSelector implements MessageSelector {

    /** List of header elements to match */
    private Map<String, String> matchingHeaders;
    
    /**
     * Default constructor using fields.
     */
    public HeaderMatchingMessageSelector(Map<String, String> matchingHeaders) {
        this.matchingHeaders = matchingHeaders;
    }
    
    @Override
    public boolean accept(Message<?> message) {
        MessageHeaders messageHeaders = message.getHeaders();

        Map<String, Object> citrusMessageHeaders = new HashMap<String, Object>();
        if (message.getPayload() instanceof com.consol.citrus.message.Message) {
            citrusMessageHeaders = ((com.consol.citrus.message.Message) message.getPayload()).copyHeaders();
        }

        for (Entry<String, String> matchEntry : matchingHeaders.entrySet()) {
            String namePart = matchEntry.getKey();
            
            if (!messageHeaders.containsKey(namePart) && !citrusMessageHeaders.containsKey(namePart)) {
                return false;
            }

            if (citrusMessageHeaders.containsKey(namePart) && !citrusMessageHeaders.get(namePart).equals(matchEntry.getValue())) {
                return false;
            } else if (messageHeaders.containsKey(namePart) && !messageHeaders.get(namePart).toString().equals(matchEntry.getValue())) {
                return false;
            }
        }
        
        return true;
    }

}
