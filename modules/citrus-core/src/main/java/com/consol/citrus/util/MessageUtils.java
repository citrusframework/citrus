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

import org.springframework.integration.MessageHeaders;


/**
 * Message utility class.
 * 
 * @author Christoph Deppisch
 */
public final class MessageUtils {
    
    /**
     * Prevent instantiation.
     */
    private MessageUtils() {
    }
    
    /**
     * Check if given header name belongs to Spring Integration internal headers.
     * 
     * This is given if header name starts with internal header prefix or 
     * matches one of Spring's internal header names.
     * 
     * @param headerName
     * @return
     */
    public static boolean isSpringInternalHeader(String headerName) {
        // '$' makes Citrus work with Spring Integration 2.0.x 
        // "springintegration_" makes Citrus work with Spring Integration 1.x release
        if (headerName.startsWith("springintegration_")) {
            return true;
        } else if (headerName.equals(MessageHeaders.ID)) {
            return true;
        } else if (headerName.equals(MessageHeaders.TIMESTAMP)) {
            return true;
        }
        
        return false;
    }
}
