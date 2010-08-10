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

package com.consol.citrus.adapter.handler;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class EmptyResponseProducingMessageHandlerTest {

    @Test
    public void testMessageHandler() {
        EmptyResponseProducingMessageHandler messageHandler = new EmptyResponseProducingMessageHandler();
        Message<?> response = messageHandler.handleMessage(
                MessageBuilder.withPayload("<TestMessage>Hello World!</TestMessage>").build());
        
        Assert.assertEquals(response.getPayload(), "");
    }
}
