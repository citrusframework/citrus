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

package org.citrusframework.xml.actions;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.PurgeEndpointAction;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;

public class PurgeEndpointTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadPurgeEndpoint() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/purge-endpoints.citrus.it.xml");

        MessageQueue testQueue = new DefaultMessageQueue("testQueue");
        context.getReferenceResolver().bind("testQueue", testQueue);
        context.getReferenceResolver().bind("testEndpoint", direct().asynchronous().queue(testQueue).build());
        context.getReferenceResolver().bind("testEndpoint1", direct().asynchronous().queue(testQueue).build());
        context.getReferenceResolver().bind("testEndpoint2", direct().asynchronous().queue(testQueue).build());
        context.getReferenceResolver().bind("testEndpoint3", direct().asynchronous().queue(testQueue).build());

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("PurgeEndpointTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(5L, result.getActionCount());
        Assertions.assertEquals(PurgeEndpointAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        PurgeEndpointAction action = (PurgeEndpointAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(100L, action.getReceiveTimeout());
        Assertions.assertEquals(350L, action.getSleepTime());
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertNotNull(action.getMessageSelectorMap());
        Assertions.assertEquals(0, action.getMessageSelectorMap().size());
        Assertions.assertEquals(0, action.getEndpoints().size());
        Assertions.assertEquals(3, action.getEndpointNames().size());
        Assertions.assertEquals("testEndpoint1", action.getEndpointNames().get(0));
        Assertions.assertEquals("testEndpoint2", action.getEndpointNames().get(1));
        Assertions.assertEquals("testEndpoint3", action.getEndpointNames().get(2));

        action = (PurgeEndpointAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertNotNull(action.getMessageSelectorMap());
        Assertions.assertEquals(0, action.getMessageSelectorMap().size());
        Assertions.assertEquals(0, action.getEndpoints().size());
        Assertions.assertEquals(1, action.getEndpointNames().size());
        Assertions.assertEquals("testEndpoint", action.getEndpointNames().get(0));

        action = (PurgeEndpointAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("operation = 'sayHello'", action.getMessageSelector());
        Assertions.assertEquals(0, action.getMessageSelectorMap().size());
        Assertions.assertEquals(0, action.getEndpoints().size());
        Assertions.assertEquals(1, action.getEndpointNames().size());
        Assertions.assertEquals("testEndpoint", action.getEndpointNames().get(0));

        action = (PurgeEndpointAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(500L, action.getReceiveTimeout());
        Assertions.assertEquals(100L, action.getSleepTime());
        Assertions.assertEquals("operation = 'sayHello'", action.getMessageSelector());
        Assertions.assertEquals(0, action.getMessageSelectorMap().size());
        Assertions.assertEquals(0, action.getEndpoints().size());
        Assertions.assertEquals(1, action.getEndpointNames().size());
        Assertions.assertEquals("testEndpoint", action.getEndpointNames().get(0));

        action = (PurgeEndpointAction) result.getTestAction(actionIndex);
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertEquals(2, action.getMessageSelectorMap().size());
        Assertions.assertEquals("sayHello", action.getMessageSelectorMap().get("operation"));
        Assertions.assertEquals("12345", action.getMessageSelectorMap().get("id"));
        Assertions.assertEquals(0, action.getEndpoints().size());
        Assertions.assertEquals(1, action.getEndpointNames().size());
        Assertions.assertEquals("testEndpoint1", action.getEndpointNames().get(0));
    }
}
