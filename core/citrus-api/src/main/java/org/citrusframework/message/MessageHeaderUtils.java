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

package org.citrusframework.message;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public final class MessageHeaderUtils {

    public static final String SEQUENCE_NUMBER = "sequenceNumber";
    public static final String SEQUENCE_SIZE = "sequenceSize";
    public static final String PRIORITY = "priority";

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
        } else if (headerName.equals("id")) {
            return true;
        } else if (headerName.equals("timestamp")) {
            return true;
        } else if (headerName.equals("replyChannel")) {
            return true;
        } else if (headerName.equals("errorChannel")) {
            return true;
        } else if (headerName.equals("contentType")) {
            return true;
        } else if (headerName.equals(PRIORITY)) {
            return true;
        } else if (headerName.equals("correlationId")) {
            return true;
        } else if (headerName.equals("routingSlip")) {
            return true;
        } else if (headerName.equals("duplicateMessage")) {
            return true;
        } else if (headerName.equals(SEQUENCE_NUMBER)) {
            return true;
        } else if (headerName.equals(SEQUENCE_SIZE)) {
            return true;
        } else if (headerName.equals("sequenceDetails")) {
            return true;
        } else if (headerName.equals("expirationDate")) {
            return true;
        } else if (headerName.startsWith("jms_")) {
            return true;
        }

        return false;
    }

    /**
     * Safely sets header on message builder. Some headers need to be cast to specific type such
     * as PRIORITY.
     * @param message
     * @param name
     * @param value
     */
    public static void setHeader(Message message, String name, String value) {
        if (name.equals(SEQUENCE_NUMBER)) {
            message.setHeader(SEQUENCE_NUMBER, Integer.valueOf(value));
        } else if (name.equals(SEQUENCE_SIZE)) {
            message.setHeader(SEQUENCE_SIZE, Integer.valueOf(value));
        } else if (name.equals(PRIORITY)) {
            message.setHeader(PRIORITY, Integer.valueOf(value));
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
        if (headers.containsKey(SEQUENCE_NUMBER)) {
            String number = headers.get(SEQUENCE_NUMBER).toString();
            headers.put(SEQUENCE_NUMBER, Integer.valueOf(number));
        }

        if (headers.containsKey(SEQUENCE_SIZE)) {
            String size = headers.get(SEQUENCE_SIZE).toString();
            headers.put(SEQUENCE_SIZE, Integer.valueOf(size));
        }

        if (headers.containsKey(PRIORITY)) {
            String size = headers.get(PRIORITY).toString();
            headers.put(PRIORITY, Integer.valueOf(size));
        }
    }
}
