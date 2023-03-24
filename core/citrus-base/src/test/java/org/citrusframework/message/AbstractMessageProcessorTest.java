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

package org.citrusframework.message;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.context.TestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class AbstractMessageProcessorTest extends UnitTestSupport {

    @Test
    public void testProcessMessage() {
        MessageProcessor processor = new AbstractMessageProcessor() {
            @Override
            public boolean supportsMessageType(String messageType) {
                return MessageType.XML.toString().equalsIgnoreCase(messageType);
            }

            @Override
            protected void processMessage(Message message, TestContext context) {
                message.setPayload("Processed!");
            }

            @Override
            protected String getName() {
                return "MockProcessor";
            }
        };

        Message in = new DefaultMessage("Hello Citrus!");
        in.setType(MessageType.XML.name());
        processor.process(in, context);
        Assert.assertEquals(in.getPayload(String.class), "Processed!");

        in = new DefaultMessage("Hello Citrus!");
        in.setType(MessageType.PLAINTEXT.name());
        processor.process(in, context);
        Assert.assertEquals(in.getPayload(String.class), "Hello Citrus!");
    }
}
