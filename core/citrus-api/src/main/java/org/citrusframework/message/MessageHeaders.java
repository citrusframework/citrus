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

package org.citrusframework.message;

/**
 * Citrus specific message headers.
 *
 * @author Christoph Deppisch
 */
public final class MessageHeaders {

    /**
     * Prevent instantiation.
     */
    private MessageHeaders() {
    }

    /** Common header name prefix */
    public static final String PREFIX = "citrus_";

    /** Message related header prefix */
    public static final String MESSAGE_PREFIX = PREFIX + "message_";

    /** Unique message id */
    public static final String ID = MESSAGE_PREFIX + "id";

    /** Time message was created */
    public static final String TIMESTAMP = MESSAGE_PREFIX + "timestamp";

    /** Header indicating the message type (e.g. xml, json, csv, plaintext, etc) */
    public static final String MESSAGE_TYPE = MESSAGE_PREFIX + "type";

    /** Synchronous message correlation */
    public static final String MESSAGE_CORRELATION_KEY = MESSAGE_PREFIX + "correlator";

    /** Synchronous reply to message destination name */
    public static final String MESSAGE_REPLY_TO = MESSAGE_PREFIX + "replyTo";

}
