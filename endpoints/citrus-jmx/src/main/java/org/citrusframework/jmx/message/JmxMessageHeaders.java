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

package org.citrusframework.jmx.message;

import org.citrusframework.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class JmxMessageHeaders {

    /**
     * Prevent instantiation.
     */
    private JmxMessageHeaders() {
        super();
    }

    /** Special header prefix for http transport headers in SOAP message sender */
    public static final String JMX_PREFIX = MessageHeaders.PREFIX + "jmx_";

    public static final String JMX_MBEAN = JMX_PREFIX + "mbean";
    public static final String JMX_OBJECT_DOMAIN = JMX_PREFIX + "object_domain";
    public static final String JMX_OBJECT_NAME = JMX_PREFIX + "object_name";
    public static final String JMX_ATTRIBUTE = JMX_PREFIX + "attribute";
    public static final String JMX_ATTRIBUTE_TYPE = JMX_PREFIX + "attribute_type";
    public static final String JMX_ATTRIBUTE_VALUE = JMX_PREFIX + "attribute_value";
    public static final String JMX_OPERATION = JMX_PREFIX + "operation";
    public static final String JMX_OPERATION_PARAMS = JMX_PREFIX + "operation_params";
}
