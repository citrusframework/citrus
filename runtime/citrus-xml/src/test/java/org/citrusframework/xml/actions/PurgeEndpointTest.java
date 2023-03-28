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
import org.citrusframework.actions.PurgeEndpointAction;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;

/**
 * @author Christoph Deppisch
 */
public class PurgeEndpointTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadPurgeEndpoint() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/purge-endpoints-test.xml");

        MessageQueue testQueue = new DefaultMessageQueue("testQueue");
        context.getReferenceResolver().bind("testQueue", testQueue);
        context.getReferenceResolver().bind("testEndpoint", direct().asynchronous().queue(testQueue).build());
        context.getReferenceResolver().bind("testEndpoint1", direct().asynchronous().queue(testQueue).build());
        context.getReferenceResolver().bind("testEndpoint2", direct().asynchronous().queue(testQueue).build());
        context.getReferenceResolver().bind("testEndpoint3", direct().asynchronous().queue(testQueue).build());

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "PurgeEndpointTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 5L);
        Assert.assertEquals(result.getTestAction(0).getClass(), PurgeEndpointAction.class);

        int actionIndex = 0;

        PurgeEndpointAction action = (PurgeEndpointAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getReceiveTimeout(), 100L);
        Assert.assertEquals(action.getSleepTime(), 350L);
        Assert.assertNull(action.getMessageSelector());
        Assert.assertNotNull(action.getMessageSelectorMap());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertEquals(action.getEndpoints().size(), 0);
        Assert.assertEquals(action.getEndpointNames().size(), 3);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");
        Assert.assertEquals(action.getEndpointNames().get(1), "testEndpoint2");
        Assert.assertEquals(action.getEndpointNames().get(2), "testEndpoint3");

        action = (PurgeEndpointAction) result.getTestAction(actionIndex++);
        Assert.assertNull(action.getMessageSelector());
        Assert.assertNotNull(action.getMessageSelectorMap());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertEquals(action.getEndpoints().size(), 0);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint");

        action = (PurgeEndpointAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertEquals(action.getEndpoints().size(), 0);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint");

        action = (PurgeEndpointAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getReceiveTimeout(), 500L);
        Assert.assertEquals(action.getSleepTime(), 100L);
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertEquals(action.getEndpoints().size(), 0);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint");

        action = (PurgeEndpointAction) result.getTestAction(actionIndex);
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 2);
        Assert.assertEquals(action.getMessageSelectorMap().get("operation"), "sayHello");
        Assert.assertEquals(action.getMessageSelectorMap().get("id"), "12345");
        Assert.assertEquals(action.getEndpoints().size(), 0);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");
    }

}
