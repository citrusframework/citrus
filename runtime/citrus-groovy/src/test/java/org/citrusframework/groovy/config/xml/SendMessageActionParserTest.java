/*
 * Copyright the original author or authors.
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

package org.citrusframework.groovy.config.xml;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SendMessageActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testSendMessageActionParser() {
        assertActionCount(3);
        assertActionClassAndName(SendMessageAction.class, "send");

        DefaultMessageBuilder messageBuilder;

        // 1st action
        SendMessageAction action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(2, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("Test", messageBuilder.buildMessageHeaders(context).get("header1"));
        Assertions.assertEquals("Test", messageBuilder.buildMessageHeaders(context).get("header2"));
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        // 2nd action
        action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        // 3nd action
        action = getNextTestActionFromTest();

        Assertions.assertTrue(action.isSchemaValidation());
        Assertions.assertEquals("fooSchema", action.getSchema());
        Assertions.assertEquals("fooRepository", action.getSchemaRepository());
    }
}
