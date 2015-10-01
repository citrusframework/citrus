/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.jmx.message;

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class JmxMessage extends DefaultMessage {

    /** Complete MBean name*/
    private String mbean;

    /** MBEan object domain */
    private String objectDomain;
    /** MBEan object name */
    private String objectName;

    private String attributeName;
    private String value;
    private String operation;

    /** Optional object properties */
    private Hashtable<String, String> objectProperties;

    /**
     * Empty constructor initializing with empty message payload.
     */
    public JmxMessage() {
        super();
    }

    /**
     * Constructs copy of given message.
     * @param message
     */
    public JmxMessage(Message message) {
        super(message);
    }

    /**
     * Default message using message payload.
     * @param payload
     */
    public JmxMessage(Object payload) {
        super(payload);
    }

    /**
     * Default message using message payload and headers.
     * @param payload
     * @param headers
     */
    public JmxMessage(Object payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    /**
     * Sets the Jmx mbean name.
     * @param mbean
     */
    public JmxMessage mbean(String mbean) {
        setHeader(JmxMessageHeaders.JMX_MBEAN, mbean);
        return this;
    }

    /**
     * Sets the Jmx objectDomain.
     * @param objectDomain
     */
    public JmxMessage objectDomain(String objectDomain) {
        setHeader(JmxMessageHeaders.JMX_OBJECT_DOMAIN, objectDomain);
        return this;
    }

    /**
     * Sets the Jmx objectName.
     * @param objectName
     */
    public JmxMessage objectName(String objectName) {
        setHeader(JmxMessageHeaders.JMX_OBJECT_NAME, objectName);
        return this;
    }

    /**
     * Sets the Jmx attribute.
     * @param attribute
     */
    public JmxMessage attribute(String attribute) {
        setHeader(JmxMessageHeaders.JMX_ATTRIBUTE, attribute);
        return this;
    }

    /**
     * Sets the Jmx value.
     * @param value
     */
    public JmxMessage value(String value) {
        setHeader(JmxMessageHeaders.JMX_VALUE, value);
        return this;
    }

    /**
     * Sets the Jmx operation.
     * @param operation
     */
    public JmxMessage operation(String operation) {
        setHeader(JmxMessageHeaders.JMX_OPERATION, operation);
        return this;
    }

    /**
     * Checks existence of operation header.
     * @return
     */
    public boolean hasOperation() {
        return getHeader(JmxMessageHeaders.JMX_OPERATION) != null;
    }

    /**
     * Checks existence of attribute header.
     * @return
     */
    public boolean hasAttribute() {
        return getHeader(JmxMessageHeaders.JMX_ATTRIBUTE) != null;
    }

    /**
     * Checks existence of value header.
     * @return
     */
    public boolean hasValue() {
        return getHeader(JmxMessageHeaders.JMX_VALUE) != null;
    }
}
