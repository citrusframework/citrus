/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.message;

import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.messaging.MessageHeaders;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public final class MessageHeaderUtils {

    /**
     * Private constructor prevents instantiation.
     */
    private MessageHeaderUtils() {
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
        // "springintegration_" makes Citrus work with Spring Integration 1.x release
        if (headerName.startsWith("springintegration_")) {
            return true;
        } else if (headerName.equals(MessageHeaders.ID)) {
            return true;
        } else if (headerName.equals(MessageHeaders.TIMESTAMP)) {
            return true;
        } else if (headerName.equals(MessageHeaders.REPLY_CHANNEL)) {
            return true;
        } else if (headerName.equals(MessageHeaders.ERROR_CHANNEL)) {
            return true;
        } else if (headerName.equals(IntegrationMessageHeaderAccessor.PRIORITY)) {
            return true;
        } else if (headerName.equals(IntegrationMessageHeaderAccessor.POSTPROCESS_RESULT)) {
            return true;
        } else if (headerName.equals(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER)) {
            return true;
        } else if (headerName.equals(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE)) {
            return true;
        } else if (headerName.equals(IntegrationMessageHeaderAccessor.SEQUENCE_DETAILS)) {
            return true;
        } else if (headerName.equals(IntegrationMessageHeaderAccessor.EXPIRATION_DATE)) {
            return true;
        } else if (headerName.startsWith("jms_")) {
            return true;
        }

        return false;
    }

    /**
     * Safely sets header on message builder. Some headers need to be cast to specific type such
     * as PRIORITY in {@link org.springframework.integration.IntegrationMessageHeaderAccessor}.
     * @param message
     * @param name
     * @param value
     */
    public static void setHeader(Message message, String name, String value) {
        if (name.equals(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER)) {
            message.setHeader(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER, Integer.valueOf(value));
        } else if (name.equals(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE)) {
            message.setHeader(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE, Integer.valueOf(value));
        } else if (name.equals(IntegrationMessageHeaderAccessor.PRIORITY)) {
            message.setHeader(IntegrationMessageHeaderAccessor.PRIORITY, Integer.valueOf(value));
        } else {
            message.setHeader(name, value);
        }
    }

    /**
     * Method checks all header types to meet Spring Integration type requirements. For instance
     * sequence number must be of type {@link Integer}.
     *
     * @param headers the headers to check.
     */
    public static void checkHeaderTypes(Map<String, Object> headers) {
        if (headers.containsKey(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER)) {
            String number = headers.get(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER).toString();
            headers.put(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER, Integer.valueOf(number));
        }

        if (headers.containsKey(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE)) {
            String size = headers.get(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE).toString();
            headers.put(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE, Integer.valueOf(size));
        }

        if (headers.containsKey(IntegrationMessageHeaderAccessor.PRIORITY)) {
            String size = headers.get(IntegrationMessageHeaderAccessor.PRIORITY).toString();
            headers.put(IntegrationMessageHeaderAccessor.PRIORITY, Integer.valueOf(size));
        }
    }
}
