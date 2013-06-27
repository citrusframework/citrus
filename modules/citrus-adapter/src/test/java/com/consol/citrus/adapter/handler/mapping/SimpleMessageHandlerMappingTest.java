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

package com.consol.citrus.adapter.handler.mapping;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class SimpleMessageHandlerMappingTest {

    private MessageHandler fooMessageHandler = EasyMock.createMock(MessageHandler.class);
    private MessageHandler barMessageHandler = EasyMock.createMock(MessageHandler.class);

    @Test
    public void testGetMessageHandler() throws Exception {
        SimpleMessageHandlerMapping messageHandlerMapping = new SimpleMessageHandlerMapping();

        Map<String, MessageHandler> mappings = new HashMap<String, MessageHandler>();
        mappings.put("foo", fooMessageHandler);
        mappings.put("bar", barMessageHandler);
        messageHandlerMapping.setHandlerMappings(mappings);

        Assert.assertEquals(messageHandlerMapping.getMessageHandler("foo"), fooMessageHandler);
        Assert.assertEquals(messageHandlerMapping.getMessageHandler("bar"), barMessageHandler);

        try {
            messageHandlerMapping.getMessageHandler("unknown");
            Assert.fail("Missing exception due to unknown mapping key");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to find matching message handler with mapping name 'unknown'"));
        }
    }
}
