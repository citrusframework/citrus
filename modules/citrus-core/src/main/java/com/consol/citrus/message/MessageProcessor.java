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

package com.consol.citrus.message;

import java.util.List;

import org.springframework.integration.core.Message;

/**
 * Message processor having 0-n message handlers. According to a dispatching message element
 * processor tries to delegate message processing to a message handler, that will take care 
 * of incoming message.
 * 
 * In case no proper message handler is found a default message handler takes care of message.
 * 
 * @author Christoph Deppisch
 * @since 2007
 *
 */
public interface MessageProcessor {
    /**
     * Process the request message.
     * @param message
     * @return
     */
    Message<?> processMessage(Message<?> message);

    /**
     * List of message handlers.
     * @return
     */
    List<MessageHandler> getMessageHandler();

    /**
     * Sets the message handler list.
     * @param messageHandler
     */
    void setMessageHandler(List<MessageHandler> messageHandler);

    /**
     * Get the default message handler.
     * @return
     */
    MessageHandler getDefaultMessageHandler();

    /**
     * Sets the default message handler.
     * @param messageHandler
     */
    void setDefaultMessageHandler(MessageHandler messageHandler);

    /**
     * Get dispatching element.
     * @return
     */
    String getMatchElement();

    /**
     * Sets the dispathing message element.
     * @param xpath
     */
    void setMatchElement(String xpath);
}
