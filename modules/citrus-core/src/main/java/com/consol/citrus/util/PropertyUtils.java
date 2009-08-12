package com.consol.citrus.util;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.Resource;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Util class supporting properties replacement in template files. For usage see doc generators and
 * test case creator.
 * 
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 22.04.2009
 */
public class PropertyUtils {

    private static final char propertyMarker = '@';
    
    public static String replacePropertiesInString(String line, Resource propertyResource) {
        Properties properties = new Properties();
        try {
            properties.load(propertyResource.getInputStream());
        } catch (IOException e) {
            return line;
        }

        return replacePropertiesInString(line, properties);
    }
    
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
            
            if(properties.containsKey(variableNameBuf.toString()) == false) {
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
