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

package org.citrusframework.config.xml;

import java.io.IOException;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.testng.AbstractActionParserTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testSendMessageActionParser() throws IOException {
        assertActionCount(3);
        assertActionClassAndName(SendMessageAction.class, "send");

        DefaultMessageBuilder messageBuilder;

        // 1st action
        SendMessageAction action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 2);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("header1"), "Test");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("header2"), "Test");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        // 2nd action
        action = getNextTestActionFromTest();
        messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();

        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        // 3nd action
        action = getNextTestActionFromTest();

        Assert.assertTrue(action.isSchemaValidation());
        Assert.assertEquals(action.getSchema(), "fooSchema");
        Assert.assertEquals(action.getSchemaRepository(), "fooRepository");
    }
}
