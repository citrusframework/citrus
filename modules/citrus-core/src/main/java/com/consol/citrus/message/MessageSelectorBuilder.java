/*
 * Copyright 2006-2010 ConSol* Software GmbH.
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

package com.consol.citrus.message;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MessageSelectorBuilder {
    
    private String selectorString = "";
    
    public MessageSelectorBuilder(String selectorString) {
        this.selectorString = selectorString;
    }
    
    public static MessageSelectorBuilder withString(String selectorString) {
        return new MessageSelectorBuilder(selectorString);
    }
    
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
    
    public String build() {
        return selectorString;
    }
}
