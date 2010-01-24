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

package com.consol.citrus.util;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.Resource;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Utility class supporting property replacement in template files. 
 * For usage see doc generators and test case creator.
 * 
 * @author Christoph Deppisch
 * @since 2009
 */
public class PropertyUtils {

    /** Constant marking a property in template files */
    private static final char propertyMarker = '@';
    
    /**
     * Replaces properties in string.
     * 
     * @param line
     * @param propertyResource
     * @return
     */
    public static String replacePropertiesInString(String line, Resource propertyResource) {
        Properties properties = new Properties();
        try {
            properties.load(propertyResource.getInputStream());
        } catch (IOException e) {
            return line;
        }

        return replacePropertiesInString(line, properties);
    }
    
    /**
     * Replaces properties in string.
     * 
     * @param line
     * @param properties
     * @return
     */
    public static String replacePropertiesInString(String line, Properties properties) {
        StringBuffer newStr = new StringBuffer();

        boolean isVarComplete = false;

        StringBuffer variableNameBuf = new StringBuffer();

        int startIndex = 0;
        int curIndex;
        int searchIndex;
        while ((searchIndex = line.indexOf(propertyMarker, startIndex)) != -1) {
            //first check if property Marker is escaped by '\' character
            if(searchIndex != 0 && line.charAt((searchIndex-1)) == '\\') {
                newStr.append(line.substring(startIndex, searchIndex-1));
                newStr.append(propertyMarker);
                startIndex = searchIndex + 1;
                continue;
            }
            
            isVarComplete = false;
            
            curIndex = searchIndex + 1;

            while (curIndex < line.length() && !isVarComplete) {
                if ((line.charAt(curIndex) == propertyMarker) || (curIndex+1 == line.length())) {
                    isVarComplete = true;
                }

                if (!isVarComplete) {
                    variableNameBuf.append(line.charAt(curIndex));
                }
                ++curIndex;
            }
            
            if(!properties.containsKey(variableNameBuf.toString())) {
                throw new CitrusRuntimeException("No such property '"
                        + propertyMarker + variableNameBuf.toString()
                        + propertyMarker + "'");
            }

            String value = properties.getProperty(variableNameBuf.toString(), "");

            newStr.append(line.substring(startIndex, searchIndex));
            if (value.indexOf(propertyMarker) != -1) {
                newStr.append(replacePropertiesInString(value, properties));
            } else {
                newStr.append(value);
            }

            startIndex = curIndex;

            variableNameBuf = new StringBuffer();
            isVarComplete = false;
        }

        newStr.append(line.substring(startIndex));

        return newStr.toString();
    }

}
