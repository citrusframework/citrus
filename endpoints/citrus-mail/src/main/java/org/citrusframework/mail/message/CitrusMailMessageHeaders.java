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
package org.citrusframework.mail.message;

import org.citrusframework.message.MessageHeaders;

/**
 * Citrus mail headers set during mail message processing.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class CitrusMailMessageHeaders {

    /**
     * Prevent instantiation.
     */
    private CitrusMailMessageHeaders() {
    }

    /** Special header prefix for http transport headers in SOAP message sender */
    public static final String MAIL_PREFIX = MessageHeaders.PREFIX + "mail_";

    /** Mail message id */
    public static final String MAIL_MESSAGE_ID = MAIL_PREFIX + "message_id";

    /** Mail recipient from */
    public static final String MAIL_FROM = MAIL_PREFIX + "from";
    /** Mail sender to */
    public static final String MAIL_TO = MAIL_PREFIX + "to";
    /** Mail copy recipients */
    public static final String MAIL_CC = MAIL_PREFIX + "cc";
    /** Mail blind copy recipients */
    public static final String MAIL_BCC = MAIL_PREFIX + "bcc";
    /** Mail reply to address */
    public static final String MAIL_REPLY_TO = MAIL_PREFIX + "reply_to";
    /** Mail delivery date */
    public static final String MAIL_DATE = MAIL_PREFIX + "date";
    /** Mail subject */
    public static final String MAIL_SUBJECT = MAIL_PREFIX + "subject";
    /** Mail mime type */
    public static final String MAIL_CONTENT_TYPE = MAIL_PREFIX + "content_type";

    /** Attachment file name */
    public static final String MAIL_FILENAME = MAIL_PREFIX + "filename";

}
