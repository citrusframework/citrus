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
 * Message processor having 0-n message handlers that will take care of incoming messages.
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 *
 */
public interface MessageProcessor {
    Message<?> processMessage(Message<?> message);

    List<MessageHandler> getMessageHandler();

    void setMessageHandler(List<MessageHandler> messageHandler);

    MessageHandler getDefaultMessageHandler();

    void setDefaultMessageHandler(MessageHandler messageHandler);

    String getMatchElement();

    void setMatchElement(String xpath);
}
