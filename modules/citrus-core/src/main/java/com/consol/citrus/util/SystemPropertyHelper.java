/*
 * Copyright 2006-2013 the original author or authors.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;
import java.util.Properties;


/**
 * Helper class sets one or more JVM system properties on initialization. Typically used in Spring application context
 * in order to set system properties on context startup.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class SystemPropertyHelper {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SystemPropertyHelper.class);
    
    /**
     * Constructor using single property name value pair.
     */
    public SystemPropertyHelper(String property, String value) {
        if (log.isDebugEnabled()) {
            log.debug("Setting system property: '" + property + "'='" + value + "'");
        }
        System.setProperty(property, value);
    }
    
    /**
     * Constructor using multiple property name value pairs.
     * @param properties
     */
    public SystemPropertyHelper(Properties properties) {
        for (Entry<Object, Object> entry : properties.entrySet()) {
            if (log.isDebugEnabled()) {
                log.debug("Setting system property: '" + entry.getKey() + "'='" + entry.getValue() + "'");
            }
            System.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
    }
}
