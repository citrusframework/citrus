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
 * Enumeration representing the possible result types for XPath expression evaluation.
 * 
 * @author Christoph Deppisch
 */
public enum XPathExpressionResult {
    NODE, STRING, BOOLEAN, NUMBER;
    
    public static XPathExpressionResult fromString(String value) {
        if(value.equals("string:")) {
            return STRING;  
        } else if (value.equals("node:")) {
            return NODE;
        } else if(value.equals("boolean:")) {
            return BOOLEAN;
        } else if(value.equals("number:")) {
            return NUMBER;
        } else {
            return NODE;
        }
    }
    
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
}
