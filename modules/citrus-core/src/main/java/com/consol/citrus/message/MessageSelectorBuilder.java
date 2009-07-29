package com.consol.citrus.message;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MessageSelectorBuilder {
    
    String selectorString = "";
    
    public MessageSelectorBuilder(String selectorString) {
        this.selectorString = selectorString;
    }
    
    public static MessageSelectorBuilder withString(String selectorString) {
        return new MessageSelectorBuilder(selectorString);
    }
    
    public static MessageSelectorBuilder fromKeyValueMap(Map valueMap) {
        StringBuffer buf = new StringBuffer();

        Iterator iter = valueMap.entrySet().iterator();

        if (iter.hasNext()) {
            Entry entry = (Entry) iter.next();
            String key = entry.getKey().toString();
            String value = (String)entry.getValue();

            buf.append(key + " = '" + value + "'");
        }

        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = (String)valueMap.get(key);

            buf.append(" AND " + key + " = '" + value + "'");
        }

        return new MessageSelectorBuilder(buf.toString());
    }
    
    public String build() {
        return selectorString;
    }
}
