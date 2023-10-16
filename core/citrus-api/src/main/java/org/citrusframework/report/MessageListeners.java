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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;

/**
 * @author Christoph Deppisch
 */
public class MessageListeners implements MessageListenerAware {

    /**
     * List of message listener known to Spring application context
     */
    private final List<MessageListener> messageListener = new ArrayList<>();

    /**
     * Delegate to all known message listener instances.
     *
     * @param message
     * @param context
     */
    public void onInboundMessage(Message message, TestContext context) {
        if (message != null) {
            for (MessageListener listener : messageListener) {
                listener.onInboundMessage(message, context);
            }
        }
    }

    /**
     * Delegate to all known message listener instances.
     *
     * @param message
     * @param context
     */
    public void onOutboundMessage(Message message, TestContext context) {
        if (message != null) {
            for (MessageListener listener : messageListener) {
                listener.onOutboundMessage(message, context);
            }
        }
    }

    /**
     * Save check if message listeners are present.
     *
     * @return
     */
    public boolean isEmpty() {
        return messageListener.isEmpty();
    }

    @Override
    public void addMessageListener(MessageListener listener) {
        if (!this.messageListener.contains(listener)) {
            this.messageListener.add(listener);
        }
    }

    /**
     * Obtains the messageListener.
     * @return
     */
    public List<MessageListener> getMessageListener() {
        return Collections.unmodifiableList(messageListener);
    }
}
