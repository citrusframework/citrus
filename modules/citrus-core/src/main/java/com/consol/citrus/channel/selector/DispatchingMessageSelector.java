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

import java.util.*;
import java.util.Map.Entry;

import org.springframework.integration.Message;
import org.springframework.integration.core.MessageSelector;
import org.springframework.util.Assert;

import com.consol.citrus.message.MessageSelectorBuilder;

/**
 * Message selector dispatches incoming messages to several other selector implementations
 * according to selector names.
 * 
 * By default uses {@link HeaderMatchingMessageSelector} and supports {@link RootQNameMessageSelector} and
 * {@link XPathEvaluatingMessageSelector}.
 * 
 * @author Christoph Deppisch
 * @since 1.2
 */
public class DispatchingMessageSelector implements MessageSelector {

    /** List of header elements to match */
    private Map<String, String> matchingHeaders;
    
    /**
     * Default constructor using a selector string.
     */
    public DispatchingMessageSelector(String selector) {
        matchingHeaders = MessageSelectorBuilder.withString(selector).toKeyValueMap();
        
        Assert.isTrue(matchingHeaders.size() > 0, "Invalid empty message selector");
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean accept(Message<?> message) {
        boolean success = true;
        
        Map<String, String> matchingHeadersCopy = new HashMap<String, String>();
        matchingHeadersCopy.putAll(matchingHeaders);
                
        // delegate to root QName message selector if necessary
        if (matchingHeadersCopy.containsKey(RootQNameMessageSelector.ROOT_QNAME_SELECTOR_ELEMENT)) {
            if (!(new RootQNameMessageSelector(matchingHeadersCopy.remove(RootQNameMessageSelector.ROOT_QNAME_SELECTOR_ELEMENT))).accept(message)) {
                success = false;
            }
        }
        
        //search for xpath selector name
        Set<String> foundXPathSelectors = new HashSet<String>();
        for (Entry<String, String> headerEntry : matchingHeadersCopy.entrySet()) {
            if (headerEntry.getKey().startsWith(XPathEvaluatingMessageSelector.XPATH_SELECTOR_ELEMENT)) {
                foundXPathSelectors.add(headerEntry.getKey());
                
                // delegate to xpath evaluating message selector
                if (!(new XPathEvaluatingMessageSelector(headerEntry.getKey(), headerEntry.getValue())).accept(message)) {
                    success = false;
                }
            }
        }
        
        //remove xpath selector elements after work is done
        for (String selectorName : foundXPathSelectors) {
            matchingHeadersCopy.remove(selectorName);
        }
        
        if (!new HeaderMatchingMessageSelector(matchingHeadersCopy).accept(message)) {
            success = false;
        }
        
        return success;
    }

}
