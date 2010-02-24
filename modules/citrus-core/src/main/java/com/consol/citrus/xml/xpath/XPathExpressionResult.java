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

package com.consol.citrus.xml.xpath;

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
    NODE, STRING, BOOLEAN, NUMBER;
    
    /** Prefix for XPath expressions in Citrus determining the result type */
    private static final String STRING_PREFIX = "string:";
    private static final String NUMBER_PREFIX = "number:";
    private static final String NODE_PREFIX = "node:";
    private static final String BOOLEAN_PREFIX = "boolean:";
    
    /**
     * Get the enumeration value from an expression string. According to the leading
     * prefix and a default result type the enumeration value is returned.
     * @param value
     * @return
     */
    public static XPathExpressionResult fromString(String value, XPathExpressionResult defaultResult) {
        if(value.startsWith(STRING_PREFIX)) {
            return STRING;  
        } else if (value.startsWith(NODE_PREFIX)) {
            return NODE;
        } else if(value.startsWith(BOOLEAN_PREFIX)) {
            return BOOLEAN;
        } else if(value.startsWith(NUMBER_PREFIX)) {
            return NUMBER;
        } else {
            return defaultResult;
        }
    }
    
    /**
     * Get a constant QName instance from this enumerations value.
     * @return
     */
    public QName getAsQName() {
        if(this.equals(STRING)) {
            return XPathConstants.STRING;
        } else if (this.equals(NODE)) {
            return XPathConstants.NODE;
        } else if(this.equals(BOOLEAN)) {
            return XPathConstants.BOOLEAN;
        } else if(this.equals(NUMBER)) {
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
        if(expression.startsWith(STRING_PREFIX)) {
            return expression.substring(STRING_PREFIX.length());  
        } else if (expression.startsWith(NODE_PREFIX)) {
            return expression.substring(NODE_PREFIX.length());
        } else if(expression.startsWith(BOOLEAN_PREFIX)) {
            return expression.substring(BOOLEAN_PREFIX.length());
        } else if(expression.startsWith(NUMBER_PREFIX)) {
            return expression.substring(NUMBER_PREFIX.length());
        } else {
            return expression;
        }
    }
}
