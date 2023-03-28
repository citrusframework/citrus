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

package org.citrusframework.report;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;

/**
 * @author Christoph Deppisch
 */
public interface MessageListener {

    /**
     * Invoked on inbound message event. Raw message data is passed to this listener
     * in a very early state of message processing. SOAP envelope for instance is
     * still part of this message content.
     * @param message
     * @param context
     */
    void onInboundMessage(Message message, TestContext context);

    /**
     * Invoked on outbound message event. Raw message data is passed to this listener
     * in a very late state of message processing. This means that a SOAP envelope for
     * instance is already part of this message content.
     * @param message
     * @param context
     */
    void onOutboundMessage(Message message, TestContext context);
}
