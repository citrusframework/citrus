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

package org.citrusframework.message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.citrusframework.context.TestContext;
import org.citrusframework.util.StringUtils;

/**
 * Constructs message selectors either from string value or from key value maps. Currently only AND logical combination
 * of multiple expressions is supported.
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
     * Build message selector from string expression or from key value map. Automatically replaces test variables.
     * @param messageSelector
     * @param messageSelectorMap
     * @param context
     * @return
     */
    public static String build(String messageSelector, Map<String, Object> messageSelectorMap, TestContext context) {
        if (StringUtils.hasText(messageSelector)) {
            return context.replaceDynamicContentInString(messageSelector);
        } else if (messageSelectorMap != null && !messageSelectorMap.isEmpty()) {
            return MessageSelectorBuilder.fromKeyValueMap(
                    context.resolveDynamicValuesInMap(messageSelectorMap)).build();
        }

        return "";
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

        if (selectorString.contains(" AND")) {
            String[] chunks = selectorString.split(" AND");
            for (String chunk : chunks) {
                tokens = escapeEqualsFromXpathNodeTest(chunk).split("=");
                valueMap.put(unescapeEqualsFromXpathNodeTest(tokens[0].trim()), tokens[1].trim().substring(1, tokens[1].trim().length() -1));
            }
        } else {
            tokens = escapeEqualsFromXpathNodeTest(selectorString).split("=");
            valueMap.put(unescapeEqualsFromXpathNodeTest(tokens[0].trim()), tokens[1].trim().substring(1, tokens[1].trim().length() -1));
        }

        return valueMap;
    }

    /**
     * Xpath expression can gold equals characters in node tests. We have to escape those first before evaluating the
     * message selector expression, because equals characters do
     * @param selectorExpression
     * @return
     */
    private String escapeEqualsFromXpathNodeTest(String selectorExpression) {
        String nodeTestStart = "[";
        String nodeTestEnd = "]";

        // check presence of Xpath node test first
        if (!selectorExpression.contains(nodeTestStart) || !selectorExpression.contains(nodeTestEnd)) {
            return selectorExpression; //no Xpath node test return initial value - nothing to escape
        }

        StringBuilder selectorBuilder = new StringBuilder();
        int nodeTestStartIndex = selectorExpression.indexOf(nodeTestStart);
        int nodeTestEndIndex = selectorExpression.indexOf(nodeTestEnd);
        boolean escape = false;
        for (int i = 0; i < selectorExpression.length(); i++) {

            if (i == nodeTestStartIndex) {
                escape = true;
            }

            if (escape && selectorExpression.charAt(i) == '=') {
                selectorBuilder.append("@equals@");
            } else {
                selectorBuilder.append(selectorExpression.charAt(i));
            }

            if (i == nodeTestEndIndex) {
                nodeTestStartIndex = selectorExpression.indexOf(nodeTestStart);
                nodeTestEndIndex = selectorExpression.indexOf(nodeTestEnd);
                escape = false;
            }
        }

        return selectorBuilder.toString();
    }

    /**
     * Parses expression string and replaces all equals character escapings with initial
     * equals character
     *
     * @param expression
     * @return
     */
    private String unescapeEqualsFromXpathNodeTest(String expression) {
        return expression.replaceAll("@equals@", "=");
    }

    /**
     * Builds the message selector.
     * @return
     */
    public String build() {
        return selectorString;
    }
}
