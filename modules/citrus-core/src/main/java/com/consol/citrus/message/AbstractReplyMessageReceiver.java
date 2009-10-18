/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.message;

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.core.Message;

public abstract class AbstractReplyMessageReceiver implements MessageReceiver, ReplyMessageHandler {

    private Map<String, Message<?>> replyMessages = new HashMap<String, Message<?>>();
    
    public Message<?> receive() {
        return getReplyMessage("");
    }

    public Message<?> receive(long timeout) {
        return receive();
    }

    public Message<?> receiveSelected(String selector) {
        return getReplyMessage(selector);
    }

    public Message<?> receiveSelected(String selector, long timeout) {
        return receiveSelected(selector);
    }

    public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
        replyMessages.put(correlationKey, replyMessage);
    }
    
    public void onReplyMessage(Message<?> replyMessage) {
        onReplyMessage(replyMessage, "");
    }

    public Message<?> getReplyMessage(String correlationKey) {
        return replyMessages.remove(correlationKey);
    }
}
