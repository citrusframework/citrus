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

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReceiveMessageActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testReceiveMessageActionParser() {
        assertActionCount(2);
        assertActionClassAndName(ReceiveMessageAction.class, "receive");

        DefaultMessageBuilder messageBuilder;

        // 1st action
        ReceiveMessageAction action = getNextTestActionFromTest();
        Assertions.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(2, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("Test", messageBuilder.buildMessageHeaders(context).get("header1"));
        Assertions.assertEquals("Test", messageBuilder.buildMessageHeaders(context).get("header2"));

        // 2nd action
        action = getNextTestActionFromTest();
        Assertions.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());

        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
    }
}
