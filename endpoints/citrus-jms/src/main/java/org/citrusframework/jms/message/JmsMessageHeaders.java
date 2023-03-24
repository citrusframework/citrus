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

package org.citrusframework.jms.message;

import org.citrusframework.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public final class JmsMessageHeaders {

    /**
     * Prevent instantiation.
     */
    private JmsMessageHeaders() {
    }

    public static final String JMS_PREFIX = MessageHeaders.PREFIX + "jms_";

    public static final String MESSAGE_ID = JMS_PREFIX + "messageId";

    public static final String CORRELATION_ID = JMS_PREFIX + "correlationId";

    public static final String REPLY_TO = JMS_PREFIX + "replyTo";

    public static final String REDELIVERED = JMS_PREFIX + "redelivered";

    public static final String PRIORITY = JMS_PREFIX + "priority";

    public static final String DESTINATION = JMS_PREFIX + "destination";

    public static final String DELIVERY_MODE = JMS_PREFIX + "deliveryMode";

    public static final String EXPIRATION = JMS_PREFIX + "expiration";

    public static final String TYPE = JMS_PREFIX + "type";

    public static final String TIMESTAMP = JMS_PREFIX + "timestamp";
}
