/*
 * Copyright 2006-2015 the original author or authors.
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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class AbstractMessageProcessorTest extends AbstractTestNGUnitTest {

    @Test
    public void testProcessMessage() {
        MessageProcessor processor = new AbstractMessageProcessor() {
            @Override
            public boolean supportsMessageType(String messageType) {
                return MessageType.XML.toString().equalsIgnoreCase(messageType);
            }

            @Override
            protected Message processMessage(Message message, TestContext context) {
                return new DefaultMessage("Processed!");
            }

            @Override
            protected String getName() {
                return "MockProcessor";
            }
        };

        Message in = new DefaultMessage("Hello Citrus!");
        in.setType(MessageType.XML.name());
        Message intercepted = processor.process(in, context);
        Assert.assertEquals(intercepted.getPayload(String.class), "Processed!");

        in.setType(MessageType.PLAINTEXT.name());
        intercepted = processor.process(in, context);
        Assert.assertEquals(intercepted.getPayload(String.class), "Hello Citrus!");
    }
}
