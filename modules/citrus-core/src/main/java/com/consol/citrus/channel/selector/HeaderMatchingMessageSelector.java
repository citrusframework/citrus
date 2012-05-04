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

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.core.MessageSelector;
import org.springframework.util.Assert;

import com.consol.citrus.message.MessageSelectorBuilder;

/**
 * Message selector matches one or more header elements with the message header. Only in case all 
 * matching header elements are present in message header and its value matches the expected value
 * the message is accepted.
 * 
 * @author Christoph Deppisch
 */
public class HeaderMatchingMessageSelector implements MessageSelector {

    private Map<String, String> matchingHeaders;
    
    /**
     * Default constructor using fields.
     */
    public HeaderMatchingMessageSelector(String selector) {
        this.matchingHeaders = MessageSelectorBuilder.withString(selector).toKeyValueMap();
        
        Assert.isTrue(matchingHeaders.size() > 0, "Missing matching message headers for this selector");
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean accept(Message<?> message) {
        MessageHeaders messageHeaders = message.getHeaders();
        
        for (Entry<String, String> matchEntry : matchingHeaders.entrySet()) {
            if (!messageHeaders.containsKey(matchEntry.getKey())) {
                return false;
            }
            
            if (!messageHeaders.get(matchEntry.getKey()).equals(matchEntry.getValue())) {
                return false;
            }
        }
        
        return true;
    }

}
