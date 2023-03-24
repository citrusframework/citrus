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

package org.citrusframework.xml.xpath;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

/**
 * Enumeration representing the possible result types for XPath expression evaluation. In Citrus
 * XPath expressions a prefix may determine the result type like this:
 * 
 * string://MyExpressionString/Value
 * number://MyExpressionString/Value
 * boolean://MyExpressionString/Value
 * 
 * The result type prefix is supposed to be stripped off before expression evaluation 
 * and determines the evaluation result.
 * 
 * @author Christoph Deppisch
 */
public enum XPathExpressionResult {
    NODE, NODESET, STRING, BOOLEAN, NUMBER, INTEGER;
    
    /** Prefix for XPath expressions in Citrus determining the result type */
    private static final String STRING_PREFIX = "string:";
    private static final String NUMBER_PREFIX = "number:";
    private static final String INTEGER_PREFIX = "integer:";
    private static final String NODE_PREFIX = "node:";
    private static final String NODESET_PREFIX = "node-set:";
    private static final String BOOLEAN_PREFIX = "boolean:";

    /**
     * Get the enumeration value from an expression string. According to the leading
     * prefix and a default result type the enumeration value is returned.
     * @param value
     * @return
     */
    public static XPathExpressionResult fromString(String value, XPathExpressionResult defaultResult) {
        if (value.startsWith(STRING_PREFIX)) {
            return STRING;  
        } else if (value.startsWith(NODE_PREFIX)) {
            return NODE;
        } else if (value.startsWith(NODESET_PREFIX)) {
            return NODESET;
        } else if (value.startsWith(BOOLEAN_PREFIX)) {
            return BOOLEAN;
        } else if (value.startsWith(NUMBER_PREFIX)) {
            return NUMBER;
        } else if (value.startsWith(INTEGER_PREFIX)) {
            return INTEGER;
        } else {
            return defaultResult;
        }
    }
    
    /**
     * Get a constant QName instance from this enumerations value.
     * @return
     */
    public QName getAsQName() {
        if (this.equals(STRING)) {
            return XPathConstants.STRING;
        } else if (this.equals(NODE)) {
            return XPathConstants.NODE;
        } else if (this.equals(NODESET)) {
            return XPathConstants.NODESET;
        } else if (this.equals(BOOLEAN)) {
            return XPathConstants.BOOLEAN;
        } else if (this.equals(NUMBER) || this.equals(INTEGER)) {
            return XPathConstants.NUMBER;
        } else {
            return XPathConstants.NODE;
        }
    }
    
    /**
     * Cut off the leading result type prefix in a XPath expression string.
     * @param expression
     * @return
     */
    public static String cutOffPrefix(String expression) {
        if (expression.startsWith(STRING_PREFIX)) {
            return expression.substring(STRING_PREFIX.length());  
        } else if (expression.startsWith(NODE_PREFIX)) {
            return expression.substring(NODE_PREFIX.length());
        } else if (expression.startsWith(NODESET_PREFIX)) {
            return expression.substring(NODESET_PREFIX.length());
        } else if (expression.startsWith(BOOLEAN_PREFIX)) {
            return expression.substring(BOOLEAN_PREFIX.length());
        } else if (expression.startsWith(NUMBER_PREFIX)) {
            return expression.substring(NUMBER_PREFIX.length());
        } else if (expression.startsWith(INTEGER_PREFIX)) {
            return expression.substring(INTEGER_PREFIX.length());
        } else {
            return expression;
        }
    }
}
