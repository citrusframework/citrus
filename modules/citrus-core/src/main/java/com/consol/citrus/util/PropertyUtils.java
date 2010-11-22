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
