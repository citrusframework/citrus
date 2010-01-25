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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class StaticResponseProducingMessageHandlerTest {

    @Test
    public void testMessageHandler() {
        StaticResponseProducingMessageHandler messageHandler = new StaticResponseProducingMessageHandler();
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("Operation", "UnitTest");
        
        messageHandler.setMessageHeader(header);
        messageHandler.setMessagePayload("<TestMessage>Hello User!</TestMessage>");
        
        Message<?> response = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestMessage>Hello World!</TestMessage>").build());
        
        Assert.assertEquals(response.getPayload(), "<TestMessage>Hello User!</TestMessage>");
        Assert.assertNotNull(response.getHeaders().get("Operation"));
        Assert.assertEquals(response.getHeaders().get("Operation"), "UnitTest");
    }
}
