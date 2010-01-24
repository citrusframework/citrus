/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
