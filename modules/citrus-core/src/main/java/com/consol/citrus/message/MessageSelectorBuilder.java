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

package com.consol.citrus.message;

import java.util.*;
import java.util.Map.Entry;

/**
 * Constructs message selectors either from string value or from key value maps.
 * 
 * @author Christoph Deppisch
 */
public class MessageSelectorBuilder {
    
    /** Selector string */
    private String selectorString = "";
    
    /**
     * Constructor using fields.
     * @param selectorString
     */
    public MessageSelectorBuilder(String selectorString) {
        this.selectorString = selectorString;
    }
    
    /**
     * Static builder method using a selector string.
     * @param selectorString
     * @return
     */
    public static MessageSelectorBuilder withString(String selectorString) {
        return new MessageSelectorBuilder(selectorString);
    }
    
    /**
     * Static builder method using a key value map.
     * @param valueMap
     * @return
     */
    public static MessageSelectorBuilder fromKeyValueMap(Map<String, Object> valueMap) {
        StringBuffer buf = new StringBuffer();

        Iterator<Entry<String, Object>> iter = valueMap.entrySet().iterator();

        if (iter.hasNext()) {
            Entry<String, Object> entry = iter.next();
            String key = entry.getKey();
            String value = entry.getValue().toString();

            buf.append(key + " = '" + value + "'");
        }

        while (iter.hasNext()) {
        	Entry<String, Object> entry = iter.next();
        	String key = entry.getKey();
            String value = entry.getValue().toString();

            buf.append(" AND " + key + " = '" + value + "'");
        }

        return new MessageSelectorBuilder(buf.toString());
    }
    
    /**
     * Constructs a key value map from selector string representation.
     * @return
     */
    public Map<String, String> toKeyValueMap() {
        Map<String, String> valueMap = new HashMap<String, String>();
        String[] tokens;
        
        if (selectorString.contains("AND")) {
            StringTokenizer tok = new StringTokenizer(selectorString, "AND");
            while (tok.hasMoreElements()) {
                String selectorItem = tok.nextElement().toString();
                tokens = selectorItem.split("=");
                
                valueMap.put(tokens[0].trim(), tokens[1].trim().substring(1, tokens[1].trim().length() -1));
            }
        } else {
            tokens = selectorString.split("=");
            
            valueMap.put(tokens[0].trim(), tokens[1].trim().substring(1, tokens[1].trim().length() -1));
        }
        
        return valueMap;
    }
    
    /**
     * Builds the message selector.
     * @return
     */
    public String build() {
        return selectorString;
    }
}
