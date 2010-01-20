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

package com.consol.citrus.adapter.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.message.MessageHandler;

public class StaticResponseProducingMessageHandler implements MessageHandler {
    
    private String messagePayload = "";
    
    private Map<String, Object> messageHeader = new HashMap<String, Object>();
    
    public Message<?> handleMessage(Message<?> message) {
        return MessageBuilder.withPayload(messagePayload).copyHeaders(messageHeader).build();
    }

    /**
     * @param messagePayload the messagePayload to set
     */
    public void setMessagePayload(String messagePayload) {
        this.messagePayload = messagePayload;
    }

    /**
     * @param messageHeader the messageHeader to set
     */
    public void setMessageHeader(Map<String, Object> messageHeader) {
        this.messageHeader = messageHeader;
    }

}
