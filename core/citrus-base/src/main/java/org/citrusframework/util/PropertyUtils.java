/*
 * Copyright the original author or authors.
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

package org.citrusframework.util;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;

import java.io.IOException;
import java.util.Properties;

/**
 * Utility class supporting property replacement in template files.
 * For usage see doc generators and test case creator.
 *
 * @since 2009
 */
public final class PropertyUtils {

    /** Constant marking a property in template files */
    private static final char PROPERTY_MARKER = '@';

    /**
     * Prevent instantiation.
     */
    private PropertyUtils() {
        super();
    }

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
    public static String replacePropertiesInString(final String line, Properties properties) {
        StringBuilder newStr = new StringBuilder();

        boolean isVarComplete = false;

        StringBuilder propertyName = new StringBuilder();

        int startIndex = 0;
        int curIndex;
        int searchIndex;
        while ((searchIndex = line.indexOf(PROPERTY_MARKER, startIndex)) != -1) {
            //first check if property Marker is escaped by '\' character
            if (searchIndex != 0 && line.charAt((searchIndex-1)) == '\\') {
                newStr.append(line, startIndex, searchIndex-1);
                newStr.append(PROPERTY_MARKER);
                startIndex = searchIndex + 1;
                continue;
            }

            curIndex = searchIndex + 1;

            while (curIndex < line.length() && !isVarComplete) {
                if ((line.charAt(curIndex) == PROPERTY_MARKER) || (curIndex+1 == line.length())) {
                    isVarComplete = true;
                }

                if (!isVarComplete) {
                    propertyName.append(line.charAt(curIndex));
                }
                ++curIndex;
            }

            if (!properties.containsKey(propertyName.toString())) {
                throw new CitrusRuntimeException("No such property '"
                        + PROPERTY_MARKER + propertyName + PROPERTY_MARKER + "'");
            }

            newStr.append(line, startIndex, searchIndex);
            newStr.append(properties.getProperty(propertyName.toString(), "")); // property value

            startIndex = curIndex;

            propertyName = new StringBuilder();
            isVarComplete = false;
        }

        newStr.append(line.substring(startIndex));

        return newStr.toString();
    }
}
