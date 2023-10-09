/*
 * Copyright 2006-2012 the original author or authors.
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

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Enumeration for supported message header types. Header values are able to define a type. In this case they
 * are typed header values. Message sender/receiver will try to set typed header values according to this.
 *
 * @author Christoph Deppisch
 */
public enum MessageHeaderType {
    INTEGER("integer", Integer.class),
    LONG("long", Long.class),
    FLOAT("float", Float.class),
    DOUBLE("double", Double.class),
    BYTE("byte", Byte.class),
    SHORT("short", Short.class),
    BOOLEAN("boolean", Boolean.class),
    STRING("string", String.class);

    /** Identifying prefix and suffix for typed header values */
    public static final String TYPE_PREFIX = "{";
    public static final String TYPE_SUFFIX = "}:";

    /** Properties */
    private String name;
    private Class<?> clazz;

    /**
     * Default constructor using fields.
     * @param name
     * @param clazz
     */
    private MessageHeaderType(String name, Class<?> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    /**
     * Checks if this header value is typed with matching type prefix.
     *
     * @param headerValue
     * @return
     */
    public static boolean isTyped(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return false;
        }

        for (MessageHeaderType messageType: MessageHeaderType.values()) {
            if (headerValue.startsWith(TYPE_PREFIX + messageType.getName() + TYPE_SUFFIX)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a typed header value with type and value.
     *
     * @param type
     * @param value
     * @return
     */
    public static String createTypedValue(String type, String value) {
        return TYPE_PREFIX + type + TYPE_SUFFIX + value;
    }

    /**
     * Try to find MessageHeaderType from a typed header value. The type definition
     * is located at the beginning of the header value with respective type definition
     * prefix and suffix.
     *
     * @param headerValue
     * @return
     */
    public static MessageHeaderType fromTypedValue(String headerValue) {
        String typeName = headerValue.substring(1, headerValue.indexOf(TYPE_SUFFIX));

        for (MessageHeaderType messageType: MessageHeaderType.values()) {
            if (messageType.getName().equals(typeName)) {
                return messageType;
            }
        }

        throw new CitrusRuntimeException("Unknown message header type in header value " + headerValue);
    }

    /**
     * Removes the type definition form a typed header value.
     *
     * @param headerValue
     * @return
     */
    public static String removeTypeDefinition(String headerValue) {
        if (isTyped(headerValue)) {
            return headerValue.substring(headerValue.indexOf(TYPE_SUFFIX) + TYPE_SUFFIX.length());
        } else {
            return headerValue;
        }
    }

    /**
     * Gets the name.
     * @return the name the name to get.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the clazz.
     * @return the clazz the clazz to get.
     */
    public Class<?> getHeaderClass() {
        return clazz;
    }

}
