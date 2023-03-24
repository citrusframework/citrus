/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.xml.actions;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;

/**
 * @author Christoph Deppisch
 */
public class ExpectTimeoutTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadExpectTimeout() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/expect-timeout-test.xml");

        MessageQueue helloQueue = new DefaultMessageQueue("helloQueue");
        context.getReferenceResolver().bind("helloQueue", helloQueue);
        context.getReferenceResolver().bind("helloEndpoint", direct().asynchronous().queue(helloQueue).build());

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "ExpectTimeoutTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 5L);
        Assert.assertEquals(result.getTestAction(0).getClass(), ReceiveTimeoutAction.class);

        int actionIndex = 0;

        ReceiveTimeoutAction action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getTimeout(), 1000L);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");
        Assert.assertNull(action.getMessageSelector());

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getEndpointUri(), "direct:helloQueue");

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getTimeout(), 500L);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");
        Assert.assertEquals(action.getMessageSelector(), "operation='Test'");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0L);

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getTimeout(), 1000L);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");
        Assert.assertEquals(action.getMessageSelector(), "operation='Test'");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0L);

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getTimeout(), 500L);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 1L);
        Assert.assertEquals(action.getMessageSelectorMap().get("operation"), "Test");
    }
}
